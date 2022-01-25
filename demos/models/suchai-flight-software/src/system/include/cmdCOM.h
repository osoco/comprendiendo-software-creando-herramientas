/**
 * @file  cmdCOM.h
 * @author Carlos Gonzalez C - carlgonz@uchile.cl
 * @date 2021
 * @copyright GNU Public License.
 *
 * This header contains commands related with the communication system
 */

#ifndef CMD_COM_H
#define CMD_COM_H

#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <limits.h>

#include "config.h"

#include "drivers.h"
#include "repoCommand.h"
#include "cmdTM.h"

/**
 * Fixed size of one CSP frame is 200. A frame contains not only data but also
 * information about data @see com_frame_t. COM_FRAME_MAX_LEN is the space
 * available for payload data, so we have do substract 2 bytes for the frame
 * number, 2 bytes fot the telemetry type and 4 bytes for the number of data
 * samples inside a frame.
 * COM_FRAME_MAX_LEN = 200-2*2-4 = 192 bytes max
 */
#define COM_FRAME_MAX_LEN (200 - 2*sizeof(uint16_t) - sizeof(uint32_t))

/**
 * A CSP frame structure. It contains data buffer and information about the data
 * such as the frame number, the telemetry type and the number of data samples
 * inside a frame.
 */
typedef struct __attribute__((__packed__)) com_frame{
    uint16_t nframe;        ///< Frame number
    uint8_t type;           ///< Telemetry type
    uint8_t node;           ///< Node of origin
    uint32_t ndata;         ///< Number of data samples (structs) in the frame
    /**
     * De data buffer containing @ndata structs of payload data. The structs
     * inside the buffer depends on the telemetry @type.
     */
    union{
        uint8_t data8[COM_FRAME_MAX_LEN];
        uint16_t data16[COM_FRAME_MAX_LEN / sizeof(uint16_t)];
        uint32_t data32[COM_FRAME_MAX_LEN / sizeof(uint32_t)];
    }data;
}com_frame_t;

/**
 * A CSP frame structure. It contains data buffer and information about the data
 * such as the frame number, the telemetry type and the number of data samples
 * inside a frame.
 */
typedef struct __attribute__((__packed__)) com_frame_file{
    uint16_t nframe;        ///< Frame number
    uint8_t type;           ///< Telemetry type
    uint8_t node;           ///< Node of origin
    uint16_t fileid;        ///< File id
    uint16_t total;         ///< Total frame numbers
    /**
     * De data buffer containing @ndata structs of payload data. The structs
     * inside the buffer depends on the telemetry @type.
     */
    uint8_t data[COM_FRAME_MAX_LEN];
}com_frame_file_t;

/**
 * Parameter to com_send_data. Stores the destination node and binary data.
 */
typedef struct __attribute__((__packed__)) com_data{
    uint8_t node;                       ///< destination node
    com_frame_t frame;
}com_data_t;

/**
 * Registers communications commands in the system
 */
void cmd_com_init(void);

/**
 * Send ping to node
 * @param fmt Str. Parameters format "%d"
 * @param param Str. Parameters as string: <node>. Ex: "10"
 * @param nparams Int. Number of parameters 1
 * @return CMD_OK if executed correctly or CMD_ERROR in case of errors
 */
int com_ping(char *fmt, char *param, int nparams);

/**
 * Send a message using the digi-repeater port, so its expect to receive the
 * the same message back.
 *
 * @param fmt Str. Parameters format "%d %s"
 * @param param Str. Parameters as string: "<node> <message>". Ex: "10 Hi!"
 * @param nparams Int. Number of parameters 2
 * @return CMD_OK if executed correctly or CMD_ERROR in case of errors
 */
int com_send_rpt(char *fmt, char *param, int nparams);

/**
 * Send a command to node using the port assigned to console commands. It
 * expects the confirmation code: 200.
 *
 * @param fmt Str. Parameters format "%d %s"
 * @param param Str. Parameters as string: "<node> <command> [parameters]". Ex: "10 help"
 * @param nparams Int. Number of parameters 2
 * @return CMD_OK if executed correctly or CMD_ERROR in case of errors
 */
int com_send_cmd(char *fmt, char *param, int nparams);

/**
 * Send a Telecommand (TC) frame to node. A TC frame contains several <command>
 * [parameters] pairs separated by ";" (semicolon), for example:
 *
 *      "help;send_cmd 10 help;ping 1;print_vars"
 *
 * The list of command will be parsed and queue in TaskCommunications @seealso
 * com_receive_tc
 *
 * @param fmt Str. Parameters format: "%d %n"
 * @param param Str. Parameters as string:
 *      "<node> <command> [parameters];<command> [parameters]".
 *      Ex: "10 help;ping 1"
 * @param nparams Int. Number of parameters: 1 (assumes that %n return the next
 *                parameter pointer).
 * @return CMD_OK if executed correctly or CMD_ERROR in case of errors
 *
 * @code
 *      // Create the TC frame for node 1 with 4 commands
 *      char *tc_frame = "1 ping 10;print_vars;send_status 10"
 *
 *      // Case 1: Call the command directly
 *      com_send_data("%d %s", tc_frame, 2);
 *
 *      // Case 2: Call the command from repoCommand
 *      cmd_t *send_cmd = cmd_get_str("send_tc");          // Get the command
 *      cmd_add_params(send_cmd, tc_frame); // Add params as binary data
 *      cmd_send(send_cmd);
 * @endcode
 *
 */
int com_send_tc_frame(char *fmt, char *params, int nparams);

/**
 * Sends telemetry data using CSP. Data is received in @params as binary, packed
 * in a @com_data_t structure that contains the destination node and the data.
 * Its expect the confirmation code: 200. See the usage example.
 *
 * @param fmt Str. Parameters format: "" (not used)
 * @param params com_data_t *. Pointer to a com_data_t structure.
 * @param nparams int. Number of parameters: 1
 * @return CMD_OK if executed correctly, CMD_ERROR in case of failures, or CMD_ERROR_SYNTAX in case of parameters errors
 *
 * @code
 *      // Create the data buffer
 *      com_data_t data;
 *      data.node = 10;                       // Set the destination node
 *      memset(data.data, 0, sizeof(data)-1); // Set the data to send (zeros)
 *
 *      // Case 1: Call the command directly
 *      com_send_data("", &data, 1);
 *
 *      // Case 2: Call the command from repoCommand
 *      cmd_t *send_cmd = cmd_get_str("send_data");         // Get the command
 *      cmd_add_params_raw(send_cmd, &data, sizeof(data));  // Add params as binary data
 *      cmd_send(send_cmd);
 * @endcode
 */
int com_send_data(char *fmt, char *params, int nparams);

/**
 * Auxiliary function to send data in one or several frames
 * @param node CSP destination node.
 * @param data Buffer to send
 * @param len Buffer len in bytes
 * @param type Telemetry type
 * @param n_data Number of struct of data in the buffer
 * @param n_frame Starting frame index
 * @return CMD_OK | CMD_ERROR | CMD_ERROR
 */
int _com_send_data(int node, void *data, size_t len, int type, int n_data, int n_frame);
int com_send_telemetry(int node, int port, int type, void *data, size_t n_bytes, int n_structs, int n_frame);
int com_send_debug(int node, char *data, size_t len);

/**
 * Split and send a file using CSP packets
 * @param node Destination node
 * @param name File name
 * @param data File data
 * @param n_bytes File size in bytes
 * @return CMD_OK | CMD_ERROR
 */
int com_send_file(int node, char *name, void *data, size_t n_bytes);

/**
 * Auxiliary function to convert an array of 32bit values to network (big) endian.
 * Applies htonl (csp_hton32) to each element of the array. This function
 * does not create a copy, so modifies the passed array in-memory.
 * Do not check buffer boundaries.
 *
 * @param buff Pointer to a 32 bit values array. Do not create a copy
 * @param len Number of elements in buff.
 */
void _hton32_buff(uint32_t *buff, int len);

/**
 * Auxiliary function to convert an array of 32bit values to host endian.
 * Applies ntohl (csp_ntoh32) to each element of the array. This function
 * does not create a copy, so modifies the passed array in-memory.
 * Do not check buffer boundaries.
 *
 * @param buff Pointer to a 32 bit values array. Do not create a copy
 * @param len Number of elements in buff.
 */
void _ntoh32_buff(uint32_t *buff, int len);


/**
 * Show CSP debug information, currently the route table and interfaces
 * @param fmt Not used
 * @param params Not used
 * @param nparams Not used
 * @return CMD_OK
 */
int com_debug(char *fmt, char *params, int nparams);

/**
 * Set module global variable trx_node. Future command calls will use this node
 *
 * @param fmt Str. Parameters format: "%d"
 * @param params Str. Parameters: <node>, the TRX node number
 * @param nparams Str. Number of parameters: 1
 * @return CMD_OK if executed correctly, CMD_ERROR in case of failures, or CMD_ERROR_SYNTAX in case of parameters errors.
 */
int com_set_node(char *fmt, char *params, int nparams);

/**
 * Send (and set) current time to node
 * @param fmt Str. Parameters format: "%d"
 * @param params  Str. Parameters: <node>
 * @param nparams Str. Number of parameters: 1
 * @return CMD_OK if executed correctly, CMD_ERROR in case of failures, or CMD_ERROR_SYNTAX in case of parameters errors.
 */
int com_get_node(char *fmt, char *params, int nparams);

/**
 * Get <current_time> and send obc_set_time <current_time> to <node>
 * @param fmt "%d"
 * @param params <node>
 * @param nparams 1
 * @return CMD_OK if executed correctly, CMD_ERROR in case of failures, or CMD_ERROR_SYNTAX in case of parameters errors.
 */
int com_set_time_node(char *fmt, char *params, int nparams);

/**
 * Download current TLE for <satellite_name> and send obc_set_tle <tle1>, obc_set_tle <tle2>, and obc_update_tle
 * commands to <node>
 * @param fmt "%d %s"
 * @param params <node> <satellte_name>
 * @param nparams 2
 * @return CMD_OK if executed correctly, CMD_ERROR in case of failures, or CMD_ERROR_SYNTAX in case of parameters errors.
 */
int com_set_tle_node(char *fmt, char *params, int nparams);

/**
 * Reset the TRX GND Watchdog timer at @node node by sending a CSP command to the
 * AX100_PORT_GNDWDT_RESET (9) port. This command targets the AX100 TRX.
 * If the <node> param is given, then the message is send to that node, if no
 * parameters given then the message is sent to SCH_TRX_ADDRESS node.
 *
 * @param fmt Str. Parameters format: "%d"
 * @param params Str. Parameters: [node], the TRX node number
 * @param nparams Str. Number of parameters: 0|1
 * @return CMD_OK if executed correctly, CMD_ERROR in case of failures, or CMD_ERROR_SYNTAX in case of parameters errors.
 *
 * @code
 *      // Function usage
 *      com_reset_gnd_wdt("%d", "5", 1);  // Reset gnd wdt of node 5
 *      com_reset_gnd_wdt("", "", 0);     // Reset gnd wdt of default TRX node
 *
 *      // Send reset gnd wdt to node 5
 *      cmd_t *send_cmd = cmd_get_str("com_reset_wdt"); // Get the command
 *      cmd_add_params_var(send_cmd, 5)  // Set param node to 5
 *      cmd_send(send_cmd);
 *
 *      // Send reset gnd_wdt to default SCH_TRX_ADDRESS node
 *      cmd_t *send_cmd = cmd_get_str("com_reset_wdt"); // Get the command
 *      cmd_send(send_cmd);  // Send command without parameters;
 *
 * @endcode
 */
int com_reset_wdt(char *fmt, char *params, int nparams);

/**
 * Print TRX housekeeping information
 * @warning not implemented yet
 *
 * @param fmt
 * @param params
 * @param nparams
 * @return
 */
int com_get_hk(char *fmt, char *params, int nparams);

/**
 * Get TRX settings values. The TRX has a list of parameters to set and
 * get (@see ax100_param.h and ax100_param_radio.h). Use this command to get
 * any parameter value by name. The special argument 'help' can be
 * used to print the list of available parameters.
 *
 * TABLES:  0 Running parameters
 *          1 RX parameters
 *          5 TX parameters
 *
 * @param fmt Str. Parameters format: "%d %s"
 * @param params Str. Parameters: <table> <param_name>, the parameter name
 * @param nparams Str. Number of parameters: 2
 * @return CMD_OK if executed correctly, CMD_ERROR in case of failures, or CMD_ERROR_SYNTAX in case of parameters errors.
 *
 * @code
 *      // Function usage
 *      com_get_config("%d %s", "0 help", 1);     // Print the parameter list
 *      com_get_config("%d %s", "0 csp_node", 1); // Read and print the TRX node
 *
 *      // Command usage to get a TRX parameter
 *      cmd_t *send_cmd = cmd_get_str("com_get_config"); // Get the command
 *      cmd_add_params(send_cmd, "0 tx_pwr")  // Read param "tx_pwr"
 *      cmd_send(send_cmd);
 * @endcode
 *
 */
int com_get_config(char *fmt, char *params, int nparams);

/**
 * Set TRX settings values. The TRX has a list of parameters to set and
 * get (@see ax100_param.h and ax100_param_radio.h). Use this command to set
 * any parameter value by name. The special argument 'help 0' can be
 * used to print the list of available parameters.
 *
 * TABLES:  0 Running parameters
 *          1 RX parameters
 *          5 TX parameters
 *
 * @param fmt Str. Parameters format: "%d %s %s"
 * @param params Str. Parameters: <table> <param_name> <param_value>, the parameter name
 * and value as strings.
 * @param nparams Str. Number of parameters: 3
 * @return CMD_OK if executed correctly, CMD_ERROR in case of failures, or CMD_ERROR_SYNTAX in case of parameters errors.
 *
 * @code
 *      // Function usage
 *      com_set_config("%d %s %s", "0 help 0", 3);     // Print the parameter list
 *      com_set_config("%d %s %s", "0 csp_node 5", 1); // Set and print the TRX node
 *
 *      // Command usage to set a TRX parameter
 *      cmd_t *send_cmd = cmd_get_str("com_set_config"); // Get the command
 *      cmd_add_params(send_cmd, "0 tx_pwr 0")  // Set param "tx_pwr" to 0
 *      cmd_send(send_cmd);
 * @endcode
 *
 */
int com_set_config(char *fmt, char *params, int nparams);

/* TODO: Add documentation */
int com_update_status_vars(char *fmt, char *params, int nparams);

/* TODO: ADD documentation */
int com_set_beacon(char *fmt, char *params, int nparams);

#endif /* CMD_COM_H */
