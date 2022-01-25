/*                                 SUCHAI
 *                      NANOSATELLITE FLIGHT SOFTWARE
 *
 *      Copyright 2020, Carlos Gonzalez Cortes, carlgonz@uchile.cl
 *      Copyright 2020, Tomas Opazo Toro, tomas.opazo.t@gmail.com
 *      Copyright 2020, Matias Ramirez Martinez, nicoram.mt@gmail.com
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

#include <math.h>
#include "cmdOBC.h"
#include "TLE.h"

static const char* tag = "cmdOBC";

#define TLE_BUFF_LEN 70
TLE tle;
static char tle1[TLE_BUFF_LEN]; //"1 42788U 17036Z   20054.20928660  .00001463  00000-0  64143-4 0  9996";
static char tle2[TLE_BUFF_LEN]; //"2 42788  97.3188 111.6825 0013081  74.6084 285.6598 15.23469130148339";

void cmd_obc_init(void)
{
    cmd_add("obc_ident", obc_ident, "", 0);
    cmd_add("obc_debug", obc_debug, "%d", 1);
    cmd_add("obc_reset", obc_reset, "", 0);
    cmd_add("obc_get_mem", obc_get_os_memory, "", 0);
    cmd_add("obc_set_time", obc_set_time,"%d",1);
    cmd_add("obc_get_time", obc_get_time, "%d", 1);
    cmd_add("obc_reset_wdt", obc_reset_wdt, "", 0);
    cmd_add("obc_system", obc_system, "%s", 1);
    cmd_add("obc_get_sensors", obc_get_sensors, "", 0);
    cmd_add("obc_update_status", obc_update_status, "", 0);
    cmd_add("obc_get_tle", obc_get_tle, "", 0);
    cmd_add("obc_set_tle", obc_set_tle, "%d %n", 2);
    cmd_add("obc_update_tle", obc_update_tle, "", 0);
    cmd_add("obc_prop_tle", obc_prop_tle, "%ld", 1);
    cmd_add("mtt_set_duty", obc_set_pwm_duty, "%d %d", 2);
    cmd_add("mtt_set_freq", obc_set_pwm_freq, "%d %f", 2);
    cmd_add("mtt_set_pwr", obc_pwm_pwr, "%d", 1);
}

int obc_ident(char* fmt, char* params, int nparams)
{
    printf("Name: %s\nID  : %d\nVer : %s\nNode: %d\n",
            SCH_NAME, SCH_DEVICE_ID, SCH_SW_VERSION, SCH_COMM_ADDRESS);
    return CMD_OK;
}

int obc_debug(char *fmt, char *params, int nparams)
{
    int dbg_type;
    if(params == NULL || sscanf(params, fmt, &dbg_type) != nparams)
    {
        LOGE(tag, "Parameter null");
        return CMD_SYNTAX_ERROR;
    }

    #ifdef AVR32
        switch(dbg_type)
        {
            case 0: LED_Toggle(LED0); break;
            case 1: LED_Toggle(LED1); break;
            case 2: LED_Toggle(LED2); break;
            case 3: LED_Toggle(LED3); break;
            default: LED_Toggle(LED0);
        }
    #endif
    #ifdef NANOMIND
        if(dbg_type <= GS_A3200_LED_A)
            gs_a3200_led_toggle((gs_a3200_led_t)dbg_type);
    #endif
    #ifdef ESP32
        static int level = 0;
        level = ~level;
        gpio_set_level(BLINK_GPIO, level);
    #endif
    #ifdef LINUX
        LOGV(tag, "OBC Debug (%d)", dbg_type);
    #endif
    return CMD_OK;

}

int obc_reset_wdt(char *fmt, char *params, int nparams)
{
    #ifdef NANOMIND
       wdt_clear();
    #endif
    #ifdef AVR32
       wdt_clear();
    #endif
    return CMD_OK;
}

int obc_reset(char *fmt, char *params, int nparams)
{
    printf("Resetting system NOW!!\n");

    #ifdef LINUX
        if(params != NULL && strcmp(params, "reboot")==0)
            system("sudo reboot");
        else
            exit(0);
    #endif
    #ifdef AVR32
        reset_do_soft_reset();
    #endif
    #ifdef NANOMIND
        cpu_reset();
    #endif

    /* Never get here */
    return CMD_OK;
}

int obc_get_os_memory(char *fmt, char *params, int nparams)
{

    #if defined(LINUX) || defined(NANOMIND) || defined(AVR32)
        struct mallinfo mi;
        mi = mallinfo();
        LOGR(tag, "Total non-mmapped bytes (arena):       %d", mi.arena);
        LOGR(tag, "# of free chunks (ordblks):            %d", mi.ordblks);
        LOGR(tag, "# of free fastbin blocks (smblks):     %d", mi.smblks);
        LOGR(tag, "# of mapped regions (hblks):           %d", mi.hblks);
        LOGR(tag, "Bytes in mapped regions (hblkhd):      %d", mi.hblkhd);
        LOGR(tag, "Max. total allocated space (usmblks):  %d", mi.usmblks);
        LOGR(tag, "Free bytes held in fastbins (fsmblks): %d", mi.fsmblks);
        LOGR(tag, "Total allocated space (uordblks):      %d", mi.uordblks);
        LOGR(tag, "Total free space (fordblks):           %d", mi.fordblks);
        LOGR(tag, "Topmost releasable block (keepcost):   %d", mi.keepcost);
        return CMD_OK;
    #elif defined(FREERTOS)
        size_t mem_heap = xPortGetFreeHeapSize();
        LOGR(tag, "Free RTOS memory: %d\n", (int)mem_heap);
        return CMD_OK;
    #else
        LOGE(tag, "Not implemented!");
        return CMD_ERROR;
    #endif
}

int obc_set_time(char* fmt, char* params,int nparams)
{
    int time_to_set;
    if(params == NULL || sscanf(params, fmt, &time_to_set) != nparams)
    {
        LOGE(tag, "Invalid params");
        return CMD_SYNTAX_ERROR;
    }

    int rc = dat_set_time(time_to_set);
    if (rc == 0)
        return CMD_OK;
    else
        return CMD_ERROR;
}

int obc_get_time(char *fmt, char *params, int nparams)
{
    int format = 0;
    if((params == NULL) || (sscanf(params, fmt, &format) < nparams))
    {
        format = 0;
    }

    int rc = dat_show_time(format);  // LOGR inside
    return (rc == 0) ? CMD_OK : CMD_ERROR;
}

int obc_system(char* fmt, char* params, int nparams)
{
#ifdef LINUX
    if(params != NULL)
    {
        int rc = system(params);
        if(rc < 0)
        {
            LOGR(tag, "Call to system failed! (%d)", rc)
            return CMD_ERROR;
        }
        else
        {
            LOGR(tag, "Call to system returned (%d)", rc);
            return CMD_OK;
        }
    }
    else
    {
        LOGE(tag, "Parameter null");
        return CMD_SYNTAX_ERROR;
    }
#else
    LOGW(tag, "Command not suported!");
    return CMD_ERROR;
#endif
}

int obc_set_pwm_duty(char* fmt, char* params, int nparams)
{
#ifdef NANOMIND
    int channel;
    int duty;
    if(params == NULL || sscanf(params, fmt, &channel, &duty) != nparams)
    {
        LOGW(tag, "set_pwm_duty used with invalid params!");
        return CMD_SYNTAX_ERROR;
    }

    if(channel < 0 || channel > 2 || duty < -100 || duty > 100)
    {
        LOGW(tag, "set_pwm_duty params out of range %d %d!", channel, duty);
        return CMD_SYNTAX_ERROR;
    }

    LOGR(tag, "Setting duty %d to Channel %d", duty, channel);
    gs_a3200_pwm_enable(channel);
    gs_a3200_pwm_set_duty(channel, duty);
    return CMD_OK;

#else
    LOGW(tag, "Command not supported!");
    return CMD_ERROR;
#endif
}

int obc_set_pwm_freq(char* fmt, char* params, int nparams)
{
#ifdef NANOMIND
    int channel;
    float freq;
    
    if(params == NULL || sscanf(params, fmt, &channel, &freq) != nparams)
        return CMD_SYNTAX_ERROR;
    
    /* The pwm cant handle frequencies above 433 Hz or below 0.1 Hz */
    if(channel > 2 || channel < 0 || freq > 433.0 || freq < 0.1)
    {
        LOGW(tag, "Parameters out of range: 0 <= channel <= 2 (%d), \
             0.1 <= freq <= 433.0 (%.4f)", channel, freq);
        return CMD_SYNTAX_ERROR;
    }
    
    float actual_freq = gs_a3200_pwm_set_freq(channel, freq);
    LOGR(tag, "PWM %d Freq set to: %.4f", channel, actual_freq);
    return CMD_OK;
#else
    return CMD_ERROR;
#endif
}

int obc_pwm_pwr(char *fmt, char *params, int nparams)
{
#ifdef NANOMIND
    int enable;
    if(params == NULL || sscanf(params, fmt, &enable) != nparams)
        return CMD_SYNTAX_ERROR;
    
    /* Turn on/off power channel */
    LOGR(tag, "PWM enabled: %d", enable>0 ? 1:0);
    if(enable > 0)
        gs_a3200_pwr_switch_enable(GS_A3200_PWR_PWM);
    else
        gs_a3200_pwr_switch_disable(GS_A3200_PWR_PWM);
    return CMD_OK;
#else
    LOGW(tag, "Command not supported!");
    return CMD_ERROR;
#endif
}

int obc_get_sensors(char *fmt, char *params, int nparams)
{
#ifdef NANOMIND

    int16_t sensor1, sensor2;
    float gyro_temp;
    int result;

    gs_mpu3300_gyro_t gyro_reading;
    gs_hmc5843_data_t hmc_reading;

    /* Read board temperature sensors */
    result = gs_lm71_read_temp(GS_A3200_SPI_SLAVE_LM71_0, 100, &sensor1); //sensor1 = lm70_read_temp(1);
    result = gs_lm71_read_temp(GS_A3200_SPI_SLAVE_LM71_1, 100, &sensor2); //sensor2 = lm70_read_temp(2);

    /* Read gyroscope temperature and rate */
    gs_mpu3300_read_temp(&gyro_temp);
    gs_mpu3300_read_gyro(&gyro_reading);

    /* Read magnetometer */
    gs_hmc5843_read_single(&hmc_reading);

    /* Get sample time */
    int curr_time =  (int)time(NULL);
    int ret;

    /* Save temperature data */
    int index_temp = dat_get_system_var(data_map[temp_sensors].sys_index);
    struct temp_data data_temp = {index_temp, curr_time, (float)(sensor1/10.0), (float)(sensor2/10.0), gyro_temp};
    ret = dat_add_payload_sample(&data_temp, temp_sensors);

    if(ret != 0) {
        return CMD_ERROR;
    }

    /* Save ADCS data */
    int index_ads = dat_get_system_var(data_map[ads_sensors].sys_index);
    struct ads_data data_ads = {index_ads, curr_time,
                                gyro_reading.gyro_x, gyro_reading.gyro_y, gyro_reading.gyro_z,
                                hmc_reading.x, hmc_reading.y, hmc_reading.z};
    ret = dat_add_payload_sample(&data_ads, ads_sensors);

    if(ret != 0) {
        return CMD_ERROR;
    }

    /* Print readings */
#if LOG_LEVEL >= LOG_LVL_DEBUG
    printf("r\nCurrent_time: %lu\n", curr_time);
    printf("Temp1: %.1f, Temp2 %.1f, Gyro temp: %.2f\r\n", sensor1/10., sensor2/10., gyro_temp);
    printf("Gyro x, y, z: %f, %f, %f\r\n", gyro_reading.gyro_x, gyro_reading.gyro_y, gyro_reading.gyro_z);
    printf("Mag x, y, z: %f, %f, %f\r\n\r\n",hmc_reading.x, hmc_reading.y, hmc_reading.z);

    struct temp_data data_out_temp;
    int res = dat_get_recent_payload_sample(&data_out_temp, temp_sensors, 0);
    printf("Got values for struct temp (%f, %f, %f) cur_time: %lu in NOR FLASH\n", data_out_temp.obc_temp_1, data_out_temp.obc_temp_2, data_out_temp.obc_temp_3, data_out_temp.timestamp);

    struct ads_data data_out_ads;
    int res2 = dat_get_recent_payload_sample(&data_out_ads, ads_sensors, 0);
    printf("Got values for struct ads (%f, %f, %f, %f, %f, %f) cur_time: %lu in NOR FLASH\n", data_out_ads.acc_x, data_out_ads.acc_y, data_out_ads.acc_z, data_out_ads.mag_x, data_out_ads.mag_y, data_out_ads.mag_z, data_out_ads.timestamp);
#endif

#elif defined LINUX
    int curr_time =  (int)time(NULL);
    float systemp, millideg;
    FILE *thermal;
    int n;

    LOGD(tag, "Reading obc data in Linux \n timestamp: %d", curr_time);
    // Reading temp
    thermal = fopen("/sys/class/thermal/thermal_zone0/temp","r");
    n = fscanf(thermal,"%f",&millideg);
    fclose(thermal);
    if(n!= 1)
        return CMD_ERROR;
    // Save temp
    systemp = millideg / 1000;
    int index_temp = dat_get_system_var(data_map[temp_sensors].sys_index);
    struct temp_data data_temp = {index_temp, curr_time, systemp, systemp, systemp};
    LOGR(tag, "Temp1: %.1f, Temp2 %.1f, Temp3: %.2f", data_temp.obc_temp_1, data_temp.obc_temp_2, data_temp.obc_temp_3);
    dat_add_payload_sample(&data_temp, temp_sensors);
#endif

    return CMD_OK;
}

int obc_update_status(char *fmt, char *params, int nparams)
{
#ifdef NANOMIND
    int16_t sensor1, sensor2;
    float gyro_temp;
    int result;

    gs_mpu3300_gyro_t gyro_reading;
    gs_hmc5843_data_t hmc_reading;

    /* Read board temperature sensors */
    result = gs_lm71_read_temp(GS_A3200_SPI_SLAVE_LM71_0, 100, &sensor1); //sensor1 = lm70_read_temp(1);
    result = gs_lm71_read_temp(GS_A3200_SPI_SLAVE_LM71_1, 100, &sensor2); //sensor2 = lm70_read_temp(2);

    /* Read gyroscope temperature and rate */
    gs_mpu3300_read_temp(&gyro_temp);
    gs_mpu3300_read_gyro(&gyro_reading);

    /* Read magnetometer */
    gs_hmc5843_read_single(&hmc_reading);

    /* Set sensors status variables (fix type) */
    value temp_1;
    temp_1.f = (float)(sensor1/10.0);
    dat_set_system_var(dat_obc_temp_1, temp_1.i);

    value temp_2;
    temp_2.f = (float)(sensor2/10.0);
    dat_set_system_var(dat_obc_temp_2, temp_2.i);

    value temp_3;
    temp_3.f = gyro_temp;
    dat_set_system_var(dat_obc_temp_3, temp_3.i);

    value acc_x;
    acc_x.f = gyro_reading.gyro_x;
    dat_set_system_var(dat_ads_omega_x, acc_x.i);

    value acc_y;
    acc_y.f = gyro_reading.gyro_y;
    dat_set_system_var(dat_ads_omega_y, acc_y.i);

    value acc_z;
    acc_z.f = gyro_reading.gyro_z;
    dat_set_system_var(dat_ads_omega_z, acc_z.i);

    value mag_x;
    mag_x.f = hmc_reading.x;
    dat_set_system_var(dat_ads_mag_x, mag_x.i);

    value mag_y;
    mag_y.f = hmc_reading.y;
    dat_set_system_var(dat_ads_mag_y, mag_y.i);

    value mag_z;
    mag_z.f = hmc_reading.z;
    dat_set_system_var(dat_ads_mag_z, mag_z.i);

#if LOG_LEVEL >= LOG_LVL_INFO
    LOGR(tag, "Temp1: %.1f, Temp2 %.1f, Gyro temp: %.2f", sensor1/10., sensor2/10., gyro_temp);
    LOGR(tag, "Gyro x, y, z: %f, %f, %f", gyro_reading.gyro_x, gyro_reading.gyro_y, gyro_reading.gyro_z);
    LOGR(tag, "Mag x, y, z: %f, %f, %f",hmc_reading.x, hmc_reading.y, hmc_reading.z);
#endif
#elif defined(LINUX)
    int curr_time =  (int)time(NULL);
    float systemp, millideg;
    FILE *thermal;
    int n;

    LOGD(tag, "Reading obc data in Linux \n timestamp: %d", curr_time);
    // Reading temp
    thermal = fopen("/sys/class/thermal/thermal_zone0/temp","r");
    n = fscanf(thermal,"%f",&millideg);
    fclose(thermal);
    if(n!= 1)
        return CMD_ERROR;

    // Save temp
    systemp = millideg / 1000;
    /* Set sensors status variables (fix type) */
    value32_t temp_1;
    temp_1.f = systemp;
    dat_set_status_var(dat_obc_temp_1, temp_1);
    dat_set_status_var(dat_obc_temp_2, temp_1);
    dat_set_status_var(dat_obc_temp_3, temp_1);

    LOGR(tag, "Temp1: %.1f, Temp2 %.1f, Temp3: %.1f", temp_1.f, temp_1.f, temp_1.f);
#endif

    return CMD_OK;
}

int obc_get_tle(char *fmt, char *params, int nparams)
{
    LOGR(tag, "%s", tle1);
    LOGR(tag, "%s", tle2);
    return CMD_OK;
}

int obc_set_tle(char *fmt, char *params, int nparams)
{
    int line_n, next;

    // fmt: "%d %n", nparams: 2
    // First number is parsed @line_n, but then all the line pointed by @params
    // is copied to the corresponding TLE line. Examples of @params:

    //          1         2         3         4         5         6
    //0123456789012345678901234567890123456789012345678901234567890123456789
    //----------------------------------------------------------------------
    //1 42788U 17036Z   20054.20928660  .00001463  00000-0  64143-4 0  9996
    //2 42788  97.3188 111.6825 0013081  74.6084 285.6598 15.23469130148339
    if(params == NULL || sscanf(params, fmt, &line_n, &next) != nparams-1)
    {
        LOGE(tag, "Error parsing parameters!");
        return CMD_SYNTAX_ERROR;
    }

    if(strlen(params) != 69)
    {
        LOGE(tag, "Invalid TLE line len! (%d)", strlen(params));
        return CMD_SYNTAX_ERROR;
    }

    if(line_n == 1)
    {
        memset(tle1, 0, TLE_BUFF_LEN);
        strncpy(tle1, params, TLE_BUFF_LEN-1);
    }
    else if(line_n == 2)
    {
        memset(tle2, 0, TLE_BUFF_LEN);
        strncpy(tle2, params, TLE_BUFF_LEN-1);
    }
    else
    {
        LOGE(tag, "Invalid TLE Line parameter");
        return CMD_SYNTAX_ERROR;
    }

    return CMD_OK;
}

int obc_update_tle(char *fmt, char *params, int nparams)
{
    parseLines(&tle, tle1, tle2);
    //TODO: Check errors
    if(tle.sgp4Error != 0)
    {
        dat_set_system_var(dat_ads_tle_epoch, 0);
        return CMD_ERROR;
    }

    LOGR(tag, "TLE updated to epoch %.8f (%d)", tle.epoch, (int)(tle.epoch/1000.0));
    dat_set_system_var(dat_ads_tle_epoch, (int)(tle.epoch/1000.0));

    return CMD_OK;
}

int obc_prop_tle(char *fmt, char *params, int nparams)
{
    double r[3];  // Sat position in ECI frame
    double v[3];  // Sat velocity in ECI frame
    int ts=0;

    if(params != NULL && sscanf(params, fmt, &ts) != nparams)
        return CMD_SYNTAX_ERROR;

    if(ts == 0)
        ts = dat_get_time();

    double ts_mili = 1000.0 * (double) ts;

    double diff = (double)ts - (double)tle.epoch/1000.0;
    diff /= 60.0;

    getRVForDate(&tle, ts_mili, r, v);

    LOGD(tag, "T : %.8f - %.8f = %.8f", ts_mili/1000.0, tle.epoch/1000.0, diff);
    LOGD(tag, "R : (%.8f, %.8f, %.8f)", r[0], r[1], r[2]);
    LOGD(tag, "V : (%.8f, %.8f, %.8f)", v[0], v[1], v[2]);
    LOGD(tag, "Er: %d", tle.rec.error);

    if(tle.sgp4Error != 0)
        return CMD_ERROR;

    value32_t pos[3] = {{.f=(float)r[0]},{.f=(float)r[1]}, {.f=(float)r[2]}};
    dat_set_status_var(dat_ads_pos_x, pos[0]);
    dat_set_status_var(dat_ads_pos_y, pos[1]);
    dat_set_status_var(dat_ads_pos_z, pos[2]);
    dat_set_system_var(dat_ads_tle_last, (int)ts);

    return CMD_OK;
}
