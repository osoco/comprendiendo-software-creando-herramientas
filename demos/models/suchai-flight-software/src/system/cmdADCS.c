/*                                 SUCHAI
 *                      NANOSATELLITE FLIGHT SOFTWARE
 *
 *      Copyright 2020, Carlos Gonzalez Cortes, carlgonz@uchile.cl
 *      Copyright 2020, Camilo Rojas Milla, camrojas@uchile.cl
 *      Copyright 2020, Gustavo Diaz Huenupan, gustavo.diaz@ing.uchile.cl
 *      Copyright 2020, Elias Obreque Sepulveda, elias.obreque@uchile.cl
 *      Copyright 2020, Javier Morales Rodriguez, javiermoralesr95@gmail.com
 *      Copyright 2020, Luis Jimenez Verdugo, luis.jimenez@ing.uchile.cl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include "cmdADCS.h"

static const char* tag = "cmdADCS";

#define ADCS_PORT 7

void cmd_adcs_init(void)
{
//    cmd_add("adcs_point", adcs_point, "", 0);
    cmd_add("adcs_quat", adcs_get_quaternion, "", 0);
    cmd_add("adcs_omega", adcs_get_omega, "", 0);
    cmd_add("adcs_mag", adcs_get_mag, "", 0);
    cmd_add("adcs_do_control", adcs_control_torque, "%lf", 1);
    cmd_add("adcs_mag_moment", adcs_mag_moment, "", 0);
    cmd_add("adcs_set_target", adcs_set_target, "%lf %lf %lf %lf %lf %lf", 6);
    cmd_add("adcs_set_to_nadir", adcs_target_nadir, "", 0);
    cmd_add("adcs_detumbling_mag", adcs_detumbling_mag, "", 0);
    cmd_add("adcs_send_attitude", adcs_send_attitude, "", 0);
}

int adcs_point(char* fmt, char* params, int nparams)
{
    csp_packet_t *packet = csp_buffer_get(COM_FRAME_MAX_LEN);
    if(packet == NULL)
        return CMD_SYNTAX_ERROR;
    memset(packet->data, 0, COM_FRAME_MAX_LEN);

    vector3_t r;
    _get_sat_vector(&r, dat_ads_pos_x);

    int len = snprintf(packet->data, COM_FRAME_MAX_LEN,
                       "adcs_point_to %lf %lf %lf", r.v0, r.v1, r.v2);
    packet->length = len;
    LOGI(tag, "ADCS CMD: (%d) %s", packet->length, packet->data);

    int rc = csp_sendto(CSP_PRIO_NORM, ADCS_PORT, SCH_TRX_PORT_CMD,
                        SCH_TRX_PORT_CMD, CSP_O_NONE, packet, 1000);

    if(rc != 0)
    {
        csp_buffer_free((void *)packet);
        return CMD_ERROR;
    }

    return CMD_OK;
}

int adcs_get_quaternion(char* fmt, char* params, int nparams)
{
    char *out_buff = (char *)malloc(COM_FRAME_MAX_LEN);
    char *in_buff = (char *)malloc(COM_FRAME_MAX_LEN);
    memset(out_buff, 0, COM_FRAME_MAX_LEN);
    memset(in_buff, 0, COM_FRAME_MAX_LEN);

    int len = snprintf(out_buff, COM_FRAME_MAX_LEN, "adcs_get_quaternion 0");
    int rc = csp_transaction(CSP_PRIO_NORM, ADCS_PORT, SCH_TRX_PORT_CMD, 100,
                             out_buff, COM_FRAME_MAX_LEN, in_buff, COM_FRAME_MAX_LEN);

    if(rc == COM_FRAME_MAX_LEN)
    {
        LOGI(tag, "QUAT: %s", in_buff);
        quaternion_t q;
        rc = sscanf(in_buff, "%lf %lf %lf %lf", &q.q0, &q.q1, &q.q2, &q.q3);
        if(rc == 4)
        {
            _set_sat_quaterion(&q, dat_ads_q0);
            quaternion_t tmp;
            _get_sat_quaterion(&tmp, dat_ads_q0);
            LOGI(tag, "SAT_QUAT: %.04f, %.04f, %.04f, %.04f", tmp.q0, tmp.q1, tmp.q2, tmp.q3);
            free(out_buff);
            free(in_buff);
            return CMD_OK;
        }
        LOGE(tag, "Error reading values!")
    }
    LOGE(tag, "csp_transaction failed! (%d)", rc);

    free(out_buff);
    free(in_buff);
    return CMD_SYNTAX_ERROR;
}

int adcs_get_omega(char* fmt, char* params, int nparams)
{
#ifdef NANOMIND
    int result;
    gs_mpu3300_gyro_t gyro_reading;
    result = gs_mpu3300_read_gyro(&gyro_reading);

    if(result == 0)
    {
        vector3_t omega;
        omega.v0 = gyro_reading.gyro_x;
        omega.v1 = gyro_reading.gyro_y;
        omega.v2 = gyro_reading.gyro_z;
        _set_sat_vector(&omega, dat_ads_omega_x);
        return CMD_OK;
    }
    return CMD_ERROR;
#endif
    return CMD_OK;
}

int adcs_get_mag(char* fmt, char* params, int nparams)
{
#ifdef NANOMIND
    gs_error_t result;
    gs_hmc5843_data_t hmc_reading;
    result = gs_hmc5843_read_single(&hmc_reading);

    if(result == GS_OK)
    {
        vector3_t mag;
        mag.v0 = hmc_reading.x;
        mag.v1 = hmc_reading.y;
        mag.v2 = hmc_reading.z;
        _set_sat_vector(&mag, dat_ads_mag_x);
        return CMD_OK;
    }
    return CMD_ERROR;
#endif
    return CMD_OK;
}

int adcs_control_torque(char* fmt, char* params, int nparams)
{
    // GLOBALS
    double ctrl_cycle;
    matrix3_t I_quat;
    mat_set_diag(&I_quat, 0.00, 0.00, 0.00);
    matrix3_t P_quat;
    mat_set_diag(&P_quat, 0.001, 0.001, 0.001);
    matrix3_t P_omega;
    mat_set_diag(&P_omega, 0.003, 0.003, 0.003);

    if(params == NULL || sscanf(params, fmt, &ctrl_cycle) != nparams)
        return CMD_SYNTAX_ERROR;

    // PARAMETERS
    quaternion_t q_i2b_est; // Current quaternion. Read as from ADCS
    quaternion_t q_i2b_tar; // Target quaternion. Read as parameter
    _get_sat_quaterion(&q_i2b_est, dat_ads_q0);
    _get_sat_quaterion(&q_i2b_tar, dat_tgt_q0);
    vector3_t omega_b_est;  // Current GYRO. Read from ADCS
    _get_sat_vector(&omega_b_est, dat_ads_omega_x);
    vector3_t omega_b_tar;
    _get_sat_vector(&omega_b_tar, dat_tgt_omega_x);

//    libra::Quaternion q_b2i_est = q_i2b_est_.conjugate(); //body frame to inertial frame
//    libra::Quaternion q_i2b_now2tar = q_b2i_est * q_i2b_tar_;//q_i2b_tar_ = qi2b_est * qi2b_now2tar：クオータニオンによる2回転は積であらわされる。
//    q_i2b_now2tar.normalize();
    quaternion_t q_b2i_est;
    quat_conjugate(&q_i2b_est, &q_b2i_est);
    quaternion_t q_i2b_now2tar;
    quat_mult(&q_b2i_est, &q_i2b_tar, &q_i2b_now2tar);
    quat_normalize(&q_i2b_now2tar, NULL);

//    Vector<3> TorqueDirection;
//    TorqueDirection[0] = q_i2b_now2tar[0];
//    TorqueDirection[1] = q_i2b_now2tar[1];
//    TorqueDirection[2] = q_i2b_now2tar[2];
//    TorqueDirection = normalize(TorqueDirection);//q1,q2,q3を標準化している。つまり、p=(l,m,n)Tを求めている。
    vector3_t torq_dir;
    memcpy(torq_dir.v, q_i2b_now2tar.vec, sizeof(vector3_t));
    assert(torq_dir.v0 == q_i2b_now2tar.q0 && torq_dir.v1 == q_i2b_now2tar.q1 && torq_dir.v2 == q_i2b_now2tar.q2);
    vec_normalize(&torq_dir, NULL);

//    double AttitudeRotation = 2 * acos(q_i2b_now2tar[3]) * 180 / M_PI; //回転角θ。q_i2b_now2tar[3]は授業ではq0として扱った。
//    error_integral = ctrl_cycle_ * AttitudeRotation * TorqueDirection;
    double att_rot = 2*acos(q_i2b_now2tar.q[3]);
    vector3_t error_integral;
    vec_cons_mult(att_rot*ctrl_cycle, &torq_dir, &error_integral);

//    Vector<3> ControlTorque = (P_quat_ * (AttitudeRotation * TorqueDirection)) + (I_quat_ * error_integral) + (P_omega_ * (omega_b_tar_ - omega_b_est_));
    vector3_t torque_rot;
    vec_cons_mult(att_rot, &torq_dir, &torque_rot); //(AttitudeRotation * TorqueDirection)
    vector3_t P;
    mat_vec_mult(P_quat, torque_rot, &P);  //P_quat_ * (AttitudeRotation * TorqueDirection)
    vector3_t I;
    mat_vec_mult(I_quat, error_integral, &I); //I_quat_ * error_integral
    vector3_t omega_b;
    vector3_t P_o;
    vec_cons_mult(-1.0, &omega_b_est, NULL);
    vec_sum(omega_b_tar, omega_b_est, &omega_b);
    mat_vec_mult(P_omega, omega_b, &P_o); //P_omega_ * (omega_b_tar_ - omega_b_est_);

    vector3_t control_torque_tmp, control_torque;
    vec_sum(P, I, &control_torque_tmp);
    vec_sum(P_o, control_torque_tmp, &control_torque);

    LOGI(tag, "CTRL_TORQUE: %f, %f, %f", control_torque.v0, control_torque.v1, control_torque.v2);

    csp_packet_t *packet = csp_buffer_get(COM_FRAME_MAX_LEN);
    if(packet == NULL)
        return CMD_ERROR;

    int len = snprintf(packet->data, COM_FRAME_MAX_LEN,
                       "adcs_set_torque %.06f %.06f %.06f",
                       control_torque.v0, control_torque.v1, control_torque.v2);
    packet->length = len;
    LOGI(tag, "ADCS CMD: (%d) %s", packet->length, packet->data);

    int rc = csp_sendto(CSP_PRIO_NORM, ADCS_PORT, SCH_TRX_PORT_CMD,
                        SCH_TRX_PORT_CMD, CSP_O_NONE, packet, 100);

    if(rc != 0)
    {
        csp_buffer_free((void *)packet);
        return CMD_ERROR;
    }

    return CMD_OK;
}

int adcs_mag_moment(char* fmt, char* params, int nparams)
{
    // GLOBALS
    vector3_t rw_lower_limit;
    rw_lower_limit.v[0] = 0.0;
    rw_lower_limit.v[1] = 0.0;
    rw_lower_limit.v[2] = 0.0;
    vector3_t select_mag_dir_torque;
    select_mag_dir_torque.v[0] = 1.0;
    select_mag_dir_torque.v[1] = 1.0;
    select_mag_dir_torque.v[2] = 1.0;
    vector3_t max_mag_am2;
    max_mag_am2.v[0] = 0.35;
    max_mag_am2.v[1] = 0.35;
    max_mag_am2.v[2] = 0.35;
    matrix3_t I_c;
    mat_set_diag(&I_c, 0.035, 0.035, 0.007);
    double nT2T = 1.0e-9;

    // PARAMETERS
    vector3_t mag_earth_b_est;
    _get_sat_vector(&mag_earth_b_est, dat_ads_mag_x);
    vector3_t omega_b_est;  // Current GYRO. Read from ADCS
    _get_sat_vector(&omega_b_est, dat_ads_omega_x);
    vector3_t omega_b_tar;
    _get_sat_vector(&omega_b_tar, dat_tgt_omega_x);

    select_mag_dir_torque.v[0] = fabs(omega_b_est.v[0]) < rw_lower_limit.v[0];
    select_mag_dir_torque.v[1] = fabs(omega_b_est.v[1]) < rw_lower_limit.v[1];
    select_mag_dir_torque.v[2] = fabs(omega_b_est.v[2]) < rw_lower_limit.v[2];
    int mode = dat_get_system_var(dat_obc_opmode);
    if (mode == DAT_OBC_OPMODE_DETUMB_MAG)
    {
        select_mag_dir_torque.v[0] = 1.0;
        select_mag_dir_torque.v[1] = 1.0;
        select_mag_dir_torque.v[2] = 1.0;
    }
    vec_cons_mult(-1.0, &omega_b_tar, NULL);
    vector3_t error_angular_vel;
    vec_sum(omega_b_est, omega_b_tar, &error_angular_vel); //dw = w_b_est - w_b_tar
    vector3_t control_torque;
    vec_cons_mult(-1.0, &error_angular_vel, NULL);
    mat_vec_mult(I_c, error_angular_vel, &control_torque); //t = -I * dw
    double inv_norm_torque = 1.0;
    double norm_torque = vec_norm(control_torque);
    if (norm_torque >= pow(10.0, -9.0))
    {
        inv_norm_torque = 1.0 / norm_torque;
    }
    vec_cons_mult(inv_norm_torque, &control_torque, NULL); //t = t/||t|| = -I*dw / ||I*dw||

    vector3_t nT2T_mag_earth_b_est;
    vec_cons_mult(nT2T, &mag_earth_b_est, &nT2T_mag_earth_b_est);
    vector3_t max_torque;
    //bellow method should be named cross product
    vec_outer_product(max_mag_am2, nT2T_mag_earth_b_est, &max_torque);// t_max = m_max x B_est
    control_torque.v[0] *= select_mag_dir_torque.v[0] * fabs(max_torque.v[0]);//tx = tx*dirx*|t_max_x|
    control_torque.v[1] *= select_mag_dir_torque.v[1] * fabs(max_torque.v[1]);
    control_torque.v[2] *= select_mag_dir_torque.v[2] * fabs(max_torque.v[2]);

    double inv_b_norm2 = 1.0 / pow(nT2T * vec_norm(mag_earth_b_est), 2.0);//=1/||B_est||**2
    vector3_t control_mag_moment_temp, control_mag_moment;
    vec_outer_product(nT2T_mag_earth_b_est, control_torque, &control_mag_moment_temp);//mc* = Bxt
    vec_cons_mult(inv_b_norm2, &control_mag_moment_temp, &control_mag_moment); //mc =  Bxt / ||B_est||**2

    LOGI(tag, "CTRL_MAG_MOMENT: %f, %f, %f", control_mag_moment.v0, control_mag_moment.v1, control_mag_moment.v2);

    csp_packet_t *packet = csp_buffer_get(COM_FRAME_MAX_LEN);
    if(packet == NULL)
        return CMD_ERROR;

    int len = snprintf(packet->data, COM_FRAME_MAX_LEN,
                       "adcs_set_mag_moment %.06f %.06f %.06f",
                       control_mag_moment.v0, control_mag_moment.v1, control_mag_moment.v2);
    //Calc PWM duty cycle from magnetic moment
    uint8_t mtq_duty[3];
    //Todo: calc based on model
    double magMoment_control_norm = vec_norm(control_mag_moment);
    vector3_t magMoment_control_unit;
    if (magMoment_control_norm >= pow(10.0, -9.0))
    {
        vec_cons_mult(100.0 / magMoment_control_norm, &magMoment_control_unit, NULL); //m_hat = m/||m||
    } else
    {
        magMoment_control_unit.v[0] = 0.0;
        magMoment_control_unit.v[1] = 0.0;
        magMoment_control_unit.v[2] = 0.0;
    }
    mtq_duty[0] = (uint8_t) control_mag_moment.v[0]; //Todo:Check that this value is in the range [0, 100]
    mtq_duty[1] = (uint8_t) control_mag_moment.v[1];
    mtq_duty[2] = (uint8_t) control_mag_moment.v[2];

    //Enable MTQ's through PWM commands
    //Check when the power on cmd should be call
    cmd_t *cmd_pwm_pwr = cmd_get_str("obc_pwm_pwr");
    cmd_add_params_str(cmd_pwm_pwr, "1");
    cmd_send(cmd_pwm_pwr);

    cmd_t *cmd_set_pwm_x = cmd_get_str("obc_set_pwm_duty");
    cmd_add_params_var(cmd_set_pwm_x, mtq_duty[0], 0);//TODO:Check cmd parameters order
    cmd_send(cmd_set_pwm_x);

    cmd_t *cmd_set_pwm_y = cmd_get_str("obc_set_pwm_duty");
    cmd_add_params_var(cmd_set_pwm_y, mtq_duty[1], 1);
    cmd_send(cmd_set_pwm_y);

    cmd_t *cmd_set_pwm_z = cmd_get_str("obc_set_pwm_duty");
    cmd_add_params_var(cmd_set_pwm_z, mtq_duty[2], 2);
    cmd_send(cmd_set_pwm_z);

    packet->length = len;
    LOGI(tag, "ADCS CMD: (%d) %s", packet->length, packet->data);

    int rc = csp_sendto(CSP_PRIO_NORM, ADCS_PORT, SCH_TRX_PORT_CMD,
                        SCH_TRX_PORT_CMD, CSP_O_NONE, packet, 100);

    if(rc != 0)
    {
        csp_buffer_free((void *)packet);
        return CMD_ERROR;
    }

    return CMD_OK;
}

int adcs_set_target(char* fmt, char* params, int nparams)
{
    double rot;
    vector3_t i_tar;  // Target vector, intertial frame, read as parameter
    vector3_t b_tar;
    vector3_t b_dir;  // Face to point to, body frame
    vector3_t b_lambda;
    vector3_t omega_tar;  // Target velocity vector, body frame, read as parameter
    quaternion_t q_i2b_est;
    quaternion_t q_b2b_now2tar;
    quaternion_t q_i2b_tar; // Target quaternion, inertial to body frame. Calculate

    if(params == NULL || sscanf(params, fmt, &i_tar.v0, &i_tar.v1, &i_tar.v2, &omega_tar.v0, &omega_tar.v1, &omega_tar.v2) != nparams)
        return CMD_ERROR;
//    int p = sscanf(params, fmt, &i_tar.v0, &i_tar.v1, &i_tar.v2, &omega_tar.v0, &omega_tar.v1, &omega_tar.v2);
    LOGW(tag, fmt, i_tar.v0, i_tar.v1, i_tar.v2, omega_tar.v0, omega_tar.v1, omega_tar.v2);
    // Set Z+ [0, 0, 1] as the face to point to
    b_dir.v0 = 0.0; b_dir.v1 = 0.0; b_dir.v2 = 1.0;
    vec_normalize(&b_dir, NULL);

    // Get target vector in body frame
    _get_sat_quaterion(&q_i2b_est, dat_ads_q0);
    vec_normalize(&i_tar, NULL);
    quat_frame_conv(&q_i2b_est, &i_tar, &b_tar);
    vec_normalize(&b_tar, NULL);

    // Get I2B target quaternion
    vec_outer_product(b_dir, b_tar, &b_lambda);
    vec_normalize(&b_lambda, NULL);
    rot = acos(vec_inner_product(b_dir, b_tar));
    axis_rotation_to_quat(b_lambda, rot, &q_b2b_now2tar); //Calculate quaternion of shaft rotation
    quat_normalize(&q_b2b_now2tar, NULL);
    quat_mult(&q_i2b_est, &q_b2b_now2tar, &q_i2b_tar); //Calculate quaternion after rotation

    _set_sat_quaterion(&q_i2b_tar, dat_tgt_q0);
    _set_sat_vector(&omega_tar, dat_tgt_omega_x);

    //TODO: Remove this print
    quaternion_t _q;
    _get_sat_quaterion(&_q, dat_tgt_q0);
    LOGI(tag, "TGT QUAT: %lf %lf %lf %lf", _q.q0, _q.q1, _q.q2, _q.q3);

    return CMD_OK;
}

int adcs_target_nadir(char* fmt, char* params, int nparams)
{
    // Get Nadir vector
    vector3_t i_tar;
    _get_sat_vector(&i_tar, dat_ads_pos_x);
    vec_cons_mult(-1.0, &i_tar, NULL);
    vec_normalize(&i_tar, NULL);

    // Get required Nadir velocity
    // Target GYRO. ECI frame. For LEO sat -> nadir
    vector3_t omega_i_tar;
    omega_i_tar.v[0] = 0.00038772452510785394;
    omega_i_tar.v[1] = -0.0010289117582512489;
    omega_i_tar.v[2] = -0.00014131086334682821;
    vector3_t omega_b_tar;
    quaternion_t q_i2b_est;
    _get_sat_quaterion(&q_i2b_est, dat_ads_q0);
    quat_frame_conv(&q_i2b_est, &omega_i_tar, &omega_b_tar);

    char *_fmt = "%lf %lf %lf %lf %lf %lf";
    char _params[SCH_CMD_MAX_STR_PARAMS];
    memset(_params, 0, SCH_CMD_MAX_STR_PARAMS);
    snprintf(_params, SCH_CMD_MAX_STR_PARAMS, _fmt, i_tar.v0, i_tar.v1, i_tar.v2,
             omega_b_tar.v0, omega_b_tar.v1, omega_b_tar.v2);
    int ret = adcs_set_target(_fmt, _params, 6);

    return ret;
}

int adcs_detumbling_mag(char* fmt, char* params, int nparams)
{
    vector3_t i_tar;
    _get_sat_vector(&i_tar, dat_ads_pos_x);
    vec_cons_mult(-1.0, &i_tar, NULL);
    vec_normalize(&i_tar, NULL);

    vector3_t omega_b_tar;
    omega_b_tar.v[0] = 0.0;
    omega_b_tar.v[0] = 0.0;
    omega_b_tar.v[0] = 0.0;

    char *_fmt = "%lf %lf %lf %lf %lf %lf";
    char _params[SCH_CMD_MAX_STR_PARAMS];
    memset(_params, 0, SCH_CMD_MAX_STR_PARAMS);
    snprintf(_params, SCH_CMD_MAX_STR_PARAMS, _fmt, i_tar.v0, i_tar.v1, i_tar.v2,
             omega_b_tar.v0, omega_b_tar.v1, omega_b_tar.v2);
    int ret = adcs_set_target(_fmt, _params, 6);

    return ret;
}

int adcs_send_attitude(char* fmt, char* params, int nparams)
{
    quaternion_t q_est, q_tgt;
    _get_sat_quaterion(&q_est, dat_ads_q0);
    _get_sat_quaterion(&q_tgt, dat_tgt_q0);

    csp_packet_t *packet = csp_buffer_get(COM_FRAME_MAX_LEN);
    if(packet == NULL)
        return CMD_ERROR;

    int len = snprintf(packet->data, COM_FRAME_MAX_LEN,
                       "adcs_set_attitude %lf %lf %lf %lf %lf %lf %lf %lf",
                       q_est.q0, q_est.q1, q_est.q2, q_est.q3,
                       q_tgt.q0, q_tgt.q1, q_tgt.q2, q_tgt.q3);
    packet->length = len;
    LOGI(tag, "OBC ATT: (%d) %s", packet->length, packet->data);

    int rc = csp_sendto(CSP_PRIO_NORM, ADCS_PORT, SCH_TRX_PORT_CMD,
                        SCH_TRX_PORT_CMD, CSP_O_NONE, packet, 100);

    if(rc != 0)
    {
        csp_buffer_free((void *)packet);
        return CMD_ERROR;
    }

    return CMD_OK;
}
