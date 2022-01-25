/*                                 SUCHAI
 *                      NANOSATELLITE FLIGHT SOFTWARE
 *
 *      Copyright 2021, Carlos Gonzalez Cortes, carlgonz@uchile.cl
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

#include "cmdCOM.h"

static const char *tag = "cmdCOM";
static char trx_node = SCH_COMM_ADDRESS;

#ifdef SCH_USE_NANOCOM
static void _com_config_help(void);
static void _com_config_find(char *param_name, int table, param_table_t **param);
#endif

void cmd_com_init(void)
{
    cmd_add("com_ping", com_ping, "%d", 1);
    cmd_add("com_send_rpt", com_send_rpt, "%d %s", 2);
    cmd_add("com_send_cmd", com_send_cmd, "%d %n", 2);
    cmd_add("com_send_tc", com_send_tc_frame, "%d %n", 2);
    cmd_add("com_send_data", com_send_data, "%d %d %n", 3);
    cmd_add("com_debug", com_debug, "", 0);
    cmd_add("com_set_node", com_set_node, "%d", 1);
    cmd_add("com_get_node", com_get_node, "", 0);
    cmd_add("com_set_time_node", com_set_time_node, "%d", 1);
    cmd_add("com_set_tle_node", com_set_tle_node, "%d %s", 2);
#ifdef SCH_USE_NANOCOM
    cmd_add("com_reset_wdt", com_reset_wdt, "%d", 1);
    cmd_add("com_get_config", com_get_config, "%d %s", 2);
    cmd_add("com_set_config", com_set_config, "%d %s %s", 3);
    cmd_add("com_update_status", com_update_status_vars, "", 0);
    cmd_add("com_set_beacon", com_set_beacon, "%d %d", 2);
#endif
}

int com_ping(char *fmt, char *params, int nparams)
{
    if(params == NULL)
    {
        LOGE(tag, "Null arguments!");
        return CMD_SYNTAX_ERROR;
    }

    int node;
    if(sscanf(params, fmt, &node) == nparams)
    {
        int rc = csp_ping((uint8_t)node, 3000, 10, CSP_O_NONE);
        LOGR(tag, "Ping to %d took %d", node, rc);
        if(rc > 0)
            return CMD_OK;
        else
            return CMD_ERROR;
    }
    return CMD_SYNTAX_ERROR;
}

int com_send_rpt(char *fmt, char *params, int nparams)
{
    if(params == NULL)
    {
        LOGE(tag, "Null arguments!");
        return CMD_SYNTAX_ERROR;
    }

    int node;
    char msg[SCH_CMD_MAX_STR_PARAMS];
    memset(msg, '\0', SCH_CMD_MAX_STR_PARAMS);

    // format: <node> <string>
    if(sscanf(params, fmt, &node, msg) == nparams)
    {
        // Create a packet with the message
        size_t msg_len = strlen(msg);
        csp_packet_t *packet = csp_buffer_get(msg_len+1);
        if(packet == NULL)
        {
            LOGE(tag, "Could not allocate packet!");
            return CMD_ERROR;
        }
        packet->length = (uint16_t)(msg_len+1);
        memcpy(packet->data, msg, msg_len+1);

        // Sending message to node RPT, do not require direct answer
        int rc = csp_sendto(CSP_PRIO_NORM, (uint8_t)node, SCH_TRX_PORT_RPT,
                            SCH_TRX_PORT_RPT, CSP_O_NONE, packet, 1000);

        if(rc == 0)
        {
            LOGV(tag, "Data sent to repeater successfully. (rc: %d, re: %s)", rc, msg);
            return CMD_OK;
        }
        else
        {
            LOGE(tag, "Error sending data to repeater. (rc: %d)", rc);
            csp_buffer_free(packet);
            return CMD_ERROR;
        }
    }

    LOGE(tag, "Error parsing parameters!");
    return CMD_SYNTAX_ERROR;
}

int com_send_cmd(char *fmt, char *params, int nparams)
{
    if(params == NULL)
    {
        LOGE(tag, "Null arguments!");
        return CMD_SYNTAX_ERROR;
    }

    int node, next, n_args;
    uint8_t rep[1];
    char msg[SCH_CMD_MAX_STR_PARAMS];
    memset(msg, '\0', SCH_CMD_MAX_STR_PARAMS);

    //format: <node> <command> [parameters]
    n_args = sscanf(params, fmt, &node, &next);
    if(n_args == nparams-1 && next > 1)
    {
        strncpy(msg, params+next, (size_t)SCH_CMD_MAX_STR_PARAMS);
        LOGV(tag, "Parsed %d: %d, %s (%d))", n_args, node, msg, next);

        // Sending message to node TC port and wait for response
        int rc = csp_transaction(1, (uint8_t)node, SCH_TRX_PORT_TC, 1000,
                                 (void *)msg, (int)strlen(msg), rep, 1);

        if(rc > 0 && rep[0] == 200)
        {
            LOGV(tag, "Command sent successfully. (rc: %d, re: %d)", rc, rep[0]);
            return CMD_OK;
        }
        else
        {
            LOGE(tag, "Error sending command. (rc: %d, re: %d)", rc, rep[0]);
            return CMD_ERROR;
        }
    }

    LOGE(tag, "Error parsing parameters!");
    return CMD_SYNTAX_ERROR;
}

int com_send_tc_frame(char *fmt, char *params, int nparams)
{
    if(params == NULL)
    {
        LOGE(tag, "Null arguments!");
        return CMD_SYNTAX_ERROR;
    }

    int node, next, n_args;
    uint8_t rep[1];
    char tc_frame[COM_FRAME_MAX_LEN];
    memset(tc_frame, '\0', COM_FRAME_MAX_LEN);

    //format: <node> <command> [parameters];...;<command> [parameters]
    n_args = sscanf(params, fmt, &node, &next);
    if(n_args == nparams-1 && next > 1)
    {
        strncpy(tc_frame, params+next, (size_t)COM_FRAME_MAX_LEN-1);
        LOGV(tag, "Parsed %d: %d, %s (%d))", n_args, node, tc_frame, next);
        // Sending message to node TC port and wait for response
        int rc = csp_transaction(1, (uint8_t)node, SCH_TRX_PORT_TC, 1000,
                                 (void *)tc_frame, (int)strlen(tc_frame), rep, 1);

        if(rc > 0 && rep[0] == 200)
        {
            LOGV(tag, "TC sent successfully. (rc: %d, re: %d)", rc, rep[0]);
            return CMD_OK;
        }
        else
        {
            LOGE(tag, "Error sending TC. (rc: %d, re: %d)", rc, rep[0]);
            return CMD_ERROR;
        }
    }

    LOGE(tag, "Error parsing parameters! (np: %d, n: %d)", n_args, next);
    return CMD_SYNTAX_ERROR;
}

int com_send_data(char *fmt, char *params, int nparams)
{
    int node, port, next;
    if(params == NULL || sscanf(params, fmt, &node, &port, &next) != nparams - 1)
    {
        LOGE(tag, "Invalid arguments!");
        return CMD_SYNTAX_ERROR;
    }
    if(next <= 0)
        return CMD_ERROR;

    char *data = params + next;
    int size = strlen(data);
    LOGI(tag, "Sending %s (%d) to node %d port %d", data, size, node, port);

    // Send the data buffer to node and wait 1 seg. for the confirmation
    int rc = csp_transaction(CSP_PRIO_NORM, node, port,1000, data, size, NULL, 0);
    return rc == 1 ? CMD_OK : CMD_ERROR;
}

int _com_send_data(int node, void *data, size_t len, int type, int n_data, int n_frame)
{
    int rc_conn = 0;
    int rc_send = 0;
    int nframe = n_frame;
    int size_data = (type ==TM_TYPE_PAYLOAD) ? (int)len/n_data : len;

    // New connection
    csp_conn_t *conn;
    conn = csp_connect(CSP_PRIO_NORM, node, SCH_TRX_PORT_TM, 500, CSP_O_NONE);
    assert(conn != NULL);

    // Send one or more frames
    while(len > 0)
    {
        // Create packet and frame
        csp_packet_t *packet = csp_buffer_get(sizeof(com_frame_t));
        packet->length = sizeof(com_frame_t);
        com_frame_t *frame = (com_frame_t *)(packet->data);
        frame->node = SCH_COMM_ADDRESS;
        frame->nframe = csp_hton16((uint16_t)nframe++);
        frame->type = (uint8_t)type;
        size_t sent = len < COM_FRAME_MAX_LEN ? len : COM_FRAME_MAX_LEN;
        int data_sent = n_data < COM_FRAME_MAX_LEN/size_data ? n_data : (int)sent/size_data;

        frame->ndata = (type ==TM_TYPE_PAYLOAD) ? csp_hton32((uint32_t)data_sent) : csp_hton32((uint32_t)n_data);

        memcpy(frame->data.data8, data, sent);

        // Send packet
        rc_send = csp_send(conn, packet, 500);
        if(rc_send == 0)
        {
            csp_buffer_free(packet);
            LOGE(tag, "Error sending frame! (%d)", rc_send);
            break;
        }

        // Process more data
        len -= sent;
        if (type == TM_TYPE_PAYLOAD) {
            n_data -= data_sent;
        }
        data += sent;

        if(nframe%SCH_COM_MAX_PACKETS == 0)
            osDelay(SCH_COM_TX_DELAY_MS);
    }

    // Close connection
    rc_conn = csp_close(conn);
    if(rc_conn != CSP_ERR_NONE)
        LOGE(tag, "Error closing connection! (%d)", rc_conn);

    return rc_send == 1 && rc_conn == CSP_ERR_NONE ? CMD_OK : CMD_ERROR;
}

int com_send_telemetry(int node, int port, int type, void *data, size_t n_bytes, int n_structs, int n_frame)
{
    int rc_conn = 0;
    int rc_send = 0;
    int nframe = n_frame;
    int size_data = n_bytes / n_structs;
    int structs_per_frame = COM_FRAME_MAX_LEN / size_data;

    // New connection
    csp_conn_t *conn;
    conn = csp_connect(CSP_PRIO_NORM, node, port, 500, CSP_O_NONE);
    if(conn == NULL)
        return CMD_ERROR;

    // Send one or more frames
    while(n_bytes > 0)
    {
        int structs_sent = n_structs < structs_per_frame ? n_structs : structs_per_frame;
        size_t bytes_sent = structs_sent * size_data;

        // Create packet and frame
        csp_packet_t *packet = csp_buffer_get(sizeof(com_frame_t));
        packet->length = sizeof(com_frame_t);
        com_frame_t *frame = (com_frame_t *)(packet->data);
        frame->node = SCH_COMM_ADDRESS;
        frame->nframe = csp_hton16((uint16_t)nframe++);
        frame->type = (uint8_t)type;
        frame->ndata = csp_hton32((uint32_t)structs_sent);
        memcpy(frame->data.data8, data, bytes_sent);

        // Send packet
        rc_send = csp_send(conn, packet, 500);
        if(rc_send == 0)
        {
            csp_buffer_free(packet);
            LOGE(tag, "Error sending frame! (%d)", rc_send);
            break;
        }

        // Process more data
        n_bytes -= bytes_sent;
        n_structs -= structs_sent;
        data += bytes_sent;

        if(nframe%SCH_COM_MAX_PACKETS == 0)
            osDelay(SCH_COM_TX_DELAY_MS);
    }

    // Close connection
    rc_conn = csp_close(conn);
    if(rc_conn != CSP_ERR_NONE)
        LOGE(tag, "Error closing connection! (%d)", rc_conn);

    return rc_send == 1 && rc_conn == CSP_ERR_NONE ? CMD_OK : CMD_ERROR;
}

int com_send_debug(int node, char *data, size_t len)
{
    com_send_telemetry(node, SCH_TRX_PORT_DBG_TM, 0, data, len, (int)len, 0);
}

int com_send_file(int node, char *name, void *data, size_t n_bytes)
{
    if(name == NULL || data == NULL)
        return CMD_ERROR;

    int rc_conn = 0;
    int rc_send = 0;
    int nframe = 0;
    int total_frames = (int)n_bytes % COM_FRAME_MAX_LEN ? 1 : 0;
    total_frames += (int)n_bytes / COM_FRAME_MAX_LEN;
    uint16_t fileid = (uint16_t)(rand() % USHRT_MAX);

    // New connection
    csp_conn_t *conn;
    conn = csp_connect(CSP_PRIO_NORM, node, SCH_TRX_PORT_FILE, 500, CSP_O_NONE);
    if(conn == NULL)
        return CMD_ERROR;

    // Send the first packet with the file name
    csp_packet_t *packet = csp_buffer_get(sizeof(com_frame_file_t));
    packet->length = sizeof(com_frame_file_t);
    com_frame_file_t *frame = (com_frame_file_t *)(packet->data);
    frame->node = SCH_COMM_ADDRESS;
    frame->nframe = nframe++;
    frame->type = TM_TYPE_FILE_START;
    frame->fileid = csp_hton16(fileid);
    frame->total = csp_hton16(total_frames);
    memset(frame->data, 0, COM_FRAME_MAX_LEN);
    strncpy((char *)(frame->data), name, COM_FRAME_MAX_LEN);
    // Send packet
    rc_send = csp_send(conn, packet, 500);
    if(rc_send == 0)
    {
        LOGE(tag, "Error sending frame! (%d)", rc_send);
        csp_buffer_free(packet);
        rc_conn = csp_close(conn);
        return CMD_ERROR;
    }

    // Send the file data
    while(n_bytes >= COM_FRAME_MAX_LEN)
    {
        size_t bytes_sent = COM_FRAME_MAX_LEN;
        // Create packet and frame
        csp_packet_t *packet = csp_buffer_get(sizeof(com_frame_file_t));
        packet->length = sizeof(com_frame_file_t);
        com_frame_file_t *frame = (com_frame_file_t *)(packet->data);
        frame->node = SCH_COMM_ADDRESS;
        frame->nframe = csp_hton16((uint16_t)nframe++);
        frame->type = TM_TYPE_FILE_DATA;
        frame->fileid = csp_hton16(fileid);
        frame->total = csp_hton16(total_frames);
        memcpy(frame->data, data, bytes_sent);
        // Send packet
        rc_send = csp_send(conn, packet, 500);
        if(rc_send == 0)
        {
            LOGE(tag, "Error sending frame! (%d)", rc_send);
            csp_buffer_free(packet);
            rc_conn = csp_close(conn);
            return CMD_ERROR;
        }

        // Process more data
        n_bytes -= bytes_sent;
        data += bytes_sent;

        if(nframe%SCH_COM_MAX_PACKETS == 0)
            osDelay(SCH_COM_TX_DELAY_MS);
    }

    // Send last frame
    size_t bytes_sent = n_bytes;
    packet = csp_buffer_get(sizeof(com_frame_file_t));
    packet->length = sizeof(com_frame_file_t);
    frame = (com_frame_file_t *)(packet->data);
    frame->node = SCH_COMM_ADDRESS;
    frame->nframe = csp_hton16((uint16_t)nframe++);
    frame->type = TM_TYPE_FILE_END;
    frame->fileid = csp_hton16(fileid);
    frame->total = csp_hton16(total_frames);
    memcpy(frame->data, data, bytes_sent);
    // Fill frame
    LOGI(tag, "Last frame include %d bytes!", bytes_sent);
    if(bytes_sent < COM_FRAME_MAX_LEN)
        memset(frame->data+bytes_sent, 0xAA, COM_FRAME_MAX_LEN-bytes_sent);
    // Send packet
    rc_send = csp_send(conn, packet, 500);
    if(rc_send == 0)
    {
        LOGE(tag, "Error sending frame! (%d)", rc_send);
        csp_buffer_free(packet);
        rc_conn = csp_close(conn);
        return CMD_ERROR;
    }

    // Close connection
    rc_conn = csp_close(conn);
    if(rc_conn != CSP_ERR_NONE)
        LOGE(tag, "Error closing connection! (%d)", rc_conn);

    return rc_send == 1 && rc_conn == CSP_ERR_NONE ? CMD_OK : CMD_ERROR;
}


void _hton32_buff(uint32_t *buff, int len)
{
    int i;
    for(i=0; i<len; i++)
        buff[i] = csp_hton32(buff[i]);
}

void _ntoh32_buff(uint32_t *buff, int len)
{
    int i;
    for(i=0; i<len; i++)
        buff[i] = csp_ntoh32(buff[i]);
}

int com_debug(char *fmt, char *params, int nparams)
{
    LOGD(tag, "Route table");
    csp_route_print_table();
    LOGD(tag, "Interfaces");
    csp_route_print_interfaces();
    LOGD(tag, "Connections")
    csp_conn_print_table();

    return CMD_OK;
}

int com_set_node(char *fmt, char *params, int nparams)
{
    if(params == NULL)
    {
        LOGE(tag, "Null arguments!");
        return CMD_SYNTAX_ERROR;
    }

    int node;
    if(sscanf(params, fmt, &node) == nparams)
    {
        trx_node = node;
        LOGR(tag, "TRX node set to %d", node);
        return CMD_OK;
    }
    return CMD_SYNTAX_ERROR;
}

int com_get_node(char *fmt, char *params, int nparams)
{
    LOGR(tag, "TRX Node: %d\n", trx_node);
    return CMD_OK;
}

int com_set_time_node(char *fmt, char *params, int nparams)
{
    int node;
    if(params == NULL || sscanf(params, fmt, &node) != nparams)
    {
        LOGE(tag, "Error parsing params!");
        return CMD_SYNTAX_ERROR;
    }

    char cmd[SCH_CMD_MAX_STR_NAME];
    sprintf(cmd, "%d obc_set_time %d", node, (int)dat_get_time());
    LOGI(tag, "Sending command 'com_send_cmd %s' to %d", cmd, node);
    com_send_cmd("%d %n", cmd, 2);
}

int com_set_tle_node(char *fmt, char *params, int nparams)
{
    char sat[50]; // TLE sat max name is 24
    int rc, node;
    memset(sat, 0, 50);
    // fmt: %s
    if(params == NULL || sscanf(params, fmt, &node, sat) != nparams)
    {
        LOGE(tag, "Error parsing params!");
        return CMD_SYNTAX_ERROR;
    }

    // Download cubesat TLE file
    rc = system("wget https://www.celestrak.com/NORAD/elements/cubesat.txt -O /tmp/cubesat.tle");
    if(rc < 0)
    {
        LOGW(tag, "Error downloading TLE file (%d)", rc);
        return CMD_ERROR;
    }

    // Search the required satellite tle
    char line[100];
    snprintf(line, 100, "cat /tmp/cubesat.tle | grep -A 2 %s > /tmp/%s.tle", sat, sat);
    LOGI(tag, "%s", line);
    rc = system(line);
    if(rc < 0)
    {
        LOGE(tag, "Error grep TLE for %s (%d)", sat, rc);
        return CMD_ERROR;
    }

    // Read the required TLE file
    memset(line, 0, 100);
    snprintf(line, 100, "/tmp/%s.tle", sat);
    LOGI(tag, "%s", line);
    FILE *file = fopen(line, "r");
    if(file == NULL)
    {
        LOGE(tag, "Error reading file %s", line);
    }

    char cmd[SCH_CMD_MAX_STR_NAME];
    // Read satellite name... skip
    memset(line, 0, 100);
    char *tle = fgets(line, 100, file);
    if(tle == NULL)
        return CMD_ERROR;
    LOGD(tag, line);

    // Read and send first TLE line
    memset(line, 0, 100);
    memset(cmd, 0, SCH_CMD_MAX_STR_NAME);

    tle = fgets(line, 100, file);
    if(tle == NULL)
        return CMD_ERROR;
    memset(line+69, 0, 100-69); // Clean the string from \r, \n others
    LOGD(tag, line);

    snprintf(cmd, SCH_CMD_MAX_STR_NAME, "%d obc_set_tle %s", node, line);
    LOGD(tag, cmd);
    rc = com_send_cmd("%d %n", cmd, 2);
    if(rc != CMD_OK)
        return CMD_ERROR;

    // Read and send second TLE line
    memset(line, 0, 100);
    memset(cmd, 0, SCH_CMD_MAX_STR_NAME);

    tle = fgets(line, 100, file);
    if(tle == NULL)
        return CMD_ERROR;
    memset(line+69, 0, 100-69); // Clean the string from \r, \n others
    LOGD(tag, line);

    snprintf(cmd, SCH_CMD_MAX_STR_NAME, "%d obc_set_tle %s", node, line);
    LOGD(tag, cmd);
    rc = com_send_cmd("%d %n", cmd, 2);
    if(rc != CMD_OK)
        return CMD_ERROR;

    // Send update tle command
    memset(cmd, 0, SCH_CMD_MAX_STR_NAME);
    snprintf(cmd, SCH_CMD_MAX_STR_NAME, "%d obc_update_tle", node);
    LOGD(tag, cmd);
    rc = com_send_cmd("%d %n", cmd, 2);
    if(rc != CMD_OK)
        return CMD_ERROR;

    fclose(file);

    LOGR(tag, "TLE sent ok!")
    return CMD_OK;
}

#ifdef SCH_USE_NANOCOM
int com_reset_wdt(char *fmt, char *params, int nparams)
{

    int rc, node, n_args = 0;

    // If no params received, try to reset the default trx_node node
    if(params == NULL || sscanf(params, fmt, &node) != nparams)
        node = (int)trx_node;

    // Send and empty message to GNDWDT_RESET (9) port
    rc = csp_transaction(CSP_PRIO_CRITICAL, node, AX100_PORT_GNDWDT_RESET, 1000, NULL, 0, NULL, 0);

    if(rc > 0)
    {
        LOGV(tag, "GND Reset sent successfully. (rc: %d)", rc);
        return CMD_OK;
    }
    else
    {
        LOGE(tag, "Error sending GND Reset. (rc: %d)", rc);
        return CMD_ERROR;
    }
}

int com_get_hk(char *fmt, char *params, int nparams)
{
    //TODO: Implement
    return CMD_ERROR;
}

int com_get_config(char *fmt, char *params, int nparams)
{
    int rc, n_args;
    int table;
    char param[SCH_CMD_MAX_STR_PARAMS];
    memset(param, '\0', SCH_CMD_MAX_STR_PARAMS);

    if(params == NULL)
    {
        LOGE(tag, "Null arguments!");
        return CMD_SYNTAX_ERROR;
    }

    // Format: <table> <param_name>
    n_args = sscanf(params, fmt, &table, &param);
    if(n_args == nparams)
    {
        param_table_t *param_i;

        // If param is 'help' then show the available param names
        if(strcmp(param, "help") == 0)
        {
            _com_config_help();
            return CMD_OK;
        }

        // Find the given parameter by name and get the size, index, type and
        // table; param_i is set to NULL if the parameter is not found.
        _com_config_find(param, table, &param_i);

        // Warning if the parameter name was not found
        if(param_i == NULL)
        {
            LOGW(tag, "Param %s not found in table %d", param, table);
            return CMD_ERROR;
        }

        // Actually get the parameter value
        void *out = malloc(param_i->size);
        rc = rparam_get_single(out, param_i->addr, param_i->type, param_i->size,
                table, trx_node, AX100_PORT_RPARAM, 1000);

        // Process the answer
        if(rc > 0)
        {
            char param_str[SCH_CMD_MAX_STR_PARAMS];
            param_to_string(param_i, param_str, 0, out, 1, SCH_CMD_MAX_STR_PARAMS) ;
            LOGR(tag, "Param %s (table %d): %s", param_i->name, table, param_str);
            free(out);
            return CMD_OK;
        }
        else
        {
            LOGE(tag, "Error getting parameter %s! (rc: %d)", param, rc);
            free(out);
            return CMD_ERROR;
        }
    }
    return CMD_SYNTAX_ERROR;
}

int com_set_config(char *fmt, char *params, int nparams)
{
    int rc, n_args;
    int table;
    char param[SCH_CMD_MAX_STR_PARAMS];
    char value[SCH_CMD_MAX_STR_PARAMS];
    memset(param, '\0', SCH_CMD_MAX_STR_PARAMS);
    memset(value, '\0', SCH_CMD_MAX_STR_PARAMS);

    if(params == NULL)
    {
        LOGE(tag, "Null arguments!");
        return CMD_SYNTAX_ERROR;
    }

    // Format: <param_name> <value>
    n_args = sscanf(params, fmt, &table, &param, &value);
    if(n_args == nparams)
    {
        param_table_t *param_i;

        // If param is 'help' then show the available param names
        if(strcmp(param, "help") == 0)
        {
            _com_config_help();
            return CMD_OK;
        }

        // Find the given parameter by name and get the size, index, type and
        // table; param_i is set to NULL if the parameter is not found.
        _com_config_find(param, table, &param_i);

        // Warning if the parameter name was not found
        if(param_i == NULL)
        {
            LOGW(tag, "Param %s not found in table %d!", param, table);
            return CMD_ERROR;
        }

        // Actually get the parameter value
        void *out = malloc(param_i->size);
        param_from_string(param_i, value, out);
        rc = rparam_set_single(out, param_i->addr, param_i->type, param_i->size,
                               table, trx_node, AX100_PORT_RPARAM, 1000);

        // Process the answer
        if(rc > 0)
        {
            char param_str[SCH_CMD_MAX_STR_PARAMS];
            param_to_string(param_i, param_str, 0, out, 1, SCH_CMD_MAX_STR_PARAMS);
            LOGR(tag, "Param %s (table %d) set to: %s", param_i->name, table, param_str);
            free(out);
            return CMD_OK;
        }
        else
        {
            LOGE(tag, "Error setting parameter %s! (rc: %d)", param, rc);
            free(out);
            return CMD_ERROR;
        }
    }

    return CMD_SYNTAX_ERROR;
}

int com_update_status_vars(char *fmt, char *params, int nparams)
{
    char *names[5] = {"freq", "tx_pwr", "baud", "mode", "bcn_interval"};
    int tables[5] = {AX100_PARAM_TX(0), AX100_PARAM_RUNNING, AX100_PARAM_TX(0), AX100_PARAM_TX(0), AX100_PARAM_RUNNING};
    dat_status_address_t vars[5] = {dat_com_freq, dat_com_tx_pwr, dat_com_bcn_period,
                             dat_com_mode, dat_com_bcn_period};
    int table = 0;
    param_table_t *param_i = NULL;
    int rc;

    int i = 0;
    for(i=0; i<5; i++)
    {
        // Find the given parameter by name and get the size, index, type and
        // table; param_i is set to NULL if the parameter is not found.
        table = tables[i];
        _com_config_find(names[i], table, &param_i);

        // Warning if the parameter name was not found
        if(param_i == NULL)
            LOGE(tag, "Parameter (%d) %s not found!", table, names[i]);

        // Actually get the parameter value
        void *out = malloc(param_i->size);
        rc = rparam_get_single(out, param_i->addr, param_i->type, param_i->size,
                               table, trx_node, AX100_PORT_RPARAM, 1000);

        // Process the answer, save value to status variables
        if(rc > 0)
        {
            if(param_i->size == sizeof(int))
                dat_set_system_var(vars[i], *((int *)out));
            else if(param_i->size == sizeof(uint8_t))
                dat_set_system_var(vars[i], *((uint8_t *)out));
            else
                LOGE(tag, "Error casting status variable");

            LOGR(tag, "Param %s (table %d) %d", param_i->name, table, dat_get_system_var(vars[i]));
            free(out);
        }
    }

    return CMD_OK;
}


/* Auxiliary functions */

/**
 * Print the list of available parameters
 */
void _com_config_help(void)
{
    int i;
    LOGI(tag, "List of available TRX parameters:")
    LOGR(tag, "TABLE %d\n", AX100_PARAM_RUNNING);
    for(i=0; i<ax100_config_count; i++)
    {
        LOGR(tag, "\t%s\n", ax100_config[i].name);
    }
    LOGR(tag, "TABLE %d\n", AX100_PARAM_TX(0));
    for(i=0; i<ax100_config_tx_count; i++)
    {
        LOGR(tag, "\t%s\n", ax100_tx_config[i].name);
    }
    LOGR(tag, "TABLE %d\n", AX100_PARAM_RX);
    for(i=0; i<ax100_config_tx_count; i++)
    {
        LOGR(tag, "\t%s\n", ax100_rx_config[i].name);
    }
}

/**
 * Find the parameter table structure and table index by name. This function is
 * used before @rparam_get_single and @rparam_set_single to obtain the parameter
 * type, index and size. If not found, the @param is set to NULL.
 *
 * @param param_name Str. Parameter name
 * @param table int *. The parameter table index will be stored here.
 * @param param param_table_t *. The parameter type, size and index will be
 * stored here. If the parameter is not found, this pointer is set to NULL.
 */
void _com_config_find(char *param_name, int table, param_table_t **param)
{
    int i = 0;
    *param = NULL;


    // Find the given parameter name in the AX100 CONFIG table
    if(table == AX100_PARAM_RUNNING)
    {
        for(i=0; i < ax100_config_count; i++)
        {
            //LOGD(tag, "%d, %s\n", i, ax100_config[i].name);
            if(strcmp(param_name, ax100_config[i].name) == 0)
            {
                *param = &(ax100_config[i]);
                LOGD(tag, "%d, %d, %s\n", i, table, ax100_config[i].name);
                return;
            }
        }
    }

    // Find the given parameter name in the AX100 RX table
    if(table == AX100_PARAM_RX)
    {
        for(i = 0; i < ax100_config_rx_count; i++)
        {
            // LOGD(tag, "(rx) %d, %s\n", i, ax100_rx_config[i].name);
            if(strcmp(param_name, ax100_rx_config[i].name) == 0)
            {
                *param = &(ax100_rx_config[i]);
                LOGD(tag, "%d, %d, %s\n", i, table, ax100_rx_config[i].name);
                return;
            }
        }
    }

    // Find the given parameter name in the AX100 TX table
    if(table == AX100_PARAM_TX(0))
    {
        for(i = 0; i < ax100_config_tx_count; i++)
        {
            // LOGD(tag, "(tx) %d, %s\n", i, ax100_tx_config[i].name);
            if(strcmp(param_name, ax100_tx_config[i].name) == 0)
            {
                *param = &(ax100_tx_config[i]);
                LOGD(tag, "%d, %d, %s\n", i, table, ax100_rx_config[i].name);
                return;
            }
        }
    }

    *param = NULL;
    return;
}

int com_set_beacon(char *fmt, char *params, int nparams)
{
    int period;
    int offset;
    if(params == NULL || sscanf(params, fmt, &period, &offset) != nparams)
    {
        LOGE(tag, "Error parsing params!");
        return CMD_SYNTAX_ERROR;
    }
    dat_set_system_var(dat_com_bcn_period, period);

    char bcn_interval_configuration[32];
    memset(bcn_interval_configuration, 0, 32);
    snprintf(bcn_interval_configuration, 32, "0 bcn_interval %d", period);

    char bcn_offset_configuration[32];
    memset(bcn_offset_configuration, 0,32);
    snprintf(bcn_offset_configuration, 32, "0 bcn_holdoff %d", offset);

    int rc_interval = com_set_config("%d %s %s", bcn_interval_configuration, 3);
    int rc_offset = com_set_config("%d %s %s", bcn_offset_configuration, 3);

    if(rc_interval == CMD_OK && rc_offset == CMD_OK)
    {
        LOGR(tag, "Set beacon period: %d, offset %d", period, offset);
        return CMD_OK;
    }
    else
        return CMD_ERROR;
}
#endif //SCH_USE_NANOCOM
