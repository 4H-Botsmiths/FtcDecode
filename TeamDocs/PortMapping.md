# FTC Robot Port Mapping

This document helps track exactly what is plugged into each of the Control Hub's and Expansion Hub's ports.

**Important:** The "Driver Station Config" column shows what device type should be selected in the Driver Station configuration for each port.

---

## Control Hub Port Mapping

### Motors

| Port | Motor Name         | Driver Station Config | Description
|------|--------------------|----------------------|------------------------------------
| 0    | CH_MOTOR_0         | GoBUILDA 5203/2402/1620 (or appropriate motor) | Rear right drive motor
| 1    | CH_MOTOR_1         | GoBUILDA 5203/2402/1620 (or appropriate motor) | Rear left drive motor
| 2    | CH_MOTOR_2         | GoBUILDA 5203/2402/1620 (or appropriate motor) | Front right drive motor
| 3    | CH_MOTOR_3         | GoBUILDA 5203/2402/1620 (or appropriate motor) | Front left drive motor  

### Servos

| Port | Servo Name         | Driver Station Config | Description
|------|--------------------|----------------------|------------------------------------
| 0    | CH_SERVO_0         | Continuous Rotation Servo | Feeder left
| 1    | CH_SERVO_1         | **Full Range Servo** | **Indexer (GoBUILDA 5-turn servo 2000-0024-0502)**
| 2    | CH_SERVO_2         | (not used) | 
| 3    | CH_SERVO_3         | (not used) |
| 4    | CH_SERVO_4         | (not used) |
| 5    | CH_SERVO_5         | (not used) |

### Sensors (I2C)

| I2C Bus | Sensor Name       | Driver Station Config | Description
|---------|-------------------|----------------------|------------------------------------
| 0       | CH_I2C_0          | REV Color Sensor V3 | Color sensor left
| 1       | CH_I2C_1          | (not used) | 
| 2       | CH_I2C_2          | (not used) | 
| 3       | CH_I2C_3          | (not used) | 

### Digital Devices

| Port | Device Name        | Description
|------|--------------------|------------------------------------
| 0    |                    |
| 1    |                    |
| 2    |                    |
| 3    |                    |
| 4    |                    |
| 5    |                    |
| 6    |                    |
| 7    |                    |

### Analog Devices

| Port | Device Name        | Description
|------|--------------------|------------------------------------
| 0    |                    |
| 1    |                    |
| 2    |                    |
| 3    |                    |

### UART Ports

| UART # | Device Name        | Description
|--------|--------------------|------------------------------------
| 4      |                    |
| 2      |                    |

### RS485 Ports

| RS485 # | Device Name        | Description
|---------|--------------------|------------------------------------
| 0       |                    |
| 1       |                    |

---

## Expansion Hub Port Mapping

### Motors

| Port | Motor Name         | Driver Station Config | Description
|------|--------------------|----------------------|------------------------------------
| 0    | EH_MOTOR_0         | GoBUILDA 5203/2402/1620 (or appropriate motor) | Left shooter motor
| 1    | EH_MOTOR_1         | GoBUILDA 5203/2402/1620 (or appropriate motor) | Right shooter motor
| 2    | EH_MOTOR_2         | (not used) |
| 3    | EH_MOTOR_3         | (not used) |

### Servos

| Port | Servo Name         | Driver Station Config | Description
|------|--------------------|----------------------|------------------------------------
| 0    | EH_SERVO_0         | Continuous Rotation Servo | Feeder right
| 1    | EH_SERVO_1         | (not used) |
| 2    | EH_SERVO_2         | (not used) |
| 3    | EH_SERVO_3         | (not used) |
| 4    | EH_SERVO_4         | (not used) |
| 5    | EH_SERVO_5         | (not used) |

### Sensors (I2C)

| I2C Bus | Sensor Name       | Driver Station Config | Description
|---------|-------------------|----------------------|------------------------------------
| 0       | EH_I2C_0          | REV Color Sensor V3 | Color sensor right
| 1       | EH_I2C_1          | (not used) |
| 2       | EH_I2C_2          | (not used) |
| 3       | EH_I2C_3          | (not used) |

### Digital Devices

| Port | Device Name        | Description
|------|--------------------|------------------------------------
| 0    |                    |
| 1    |                    |
| 2    |                    |
| 3    |                    |
| 4    |                    |
| 5    |                    |
| 6    |                    |
| 7    |                    |

### Analog Devices

| Port | Device Name        | Description
|------|--------------------|------------------------------------
| 0    |                    |
| 1    |                    |
| 2    |                    |
| 3    |                    |

### UART Ports

| UART # | Device Name        | Description
|--------|--------------------|------------------------------------
| 4      |                    |
| 2      |                    |

### RS485 Ports

| RS485 # | Device Name        | Description
|---------|--------------------|------------------------------------
| 0       |                    |
| 1       |                    |
