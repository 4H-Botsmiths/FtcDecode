# FTC Robot Port Mapping

This document helps track exactly what is plugged into each of the Control Hub's and Expansion Hub's ports.

---

## Control Hub Port Mapping

### Motors

| Port | Motor Name         | Description
|------|--------------------|------------------------------------
| 0    | DRIVE_FRONT_RIGHT  | Yellow Jacket Planetary Gear Motor
| 1    | DRIVE_FRONT_LEFT   | Yellow Jacket Planetary Gear Motor
| 2    | DRIVE_REAR_RIGHT   | Yellow Jacket Planetary Gear Motor
| 3    | DRIVE_REAR_LEFT    | Yellow Jacket Planetary Gear Motor  

### Servos

| Port | Servo Name         | Description
|------|--------------------|------------------------------------
| 0    | INTAKE_WHEEL       | GoBilda Speed Servo
| 1    | INTAKE_JOINT_1     | GoBilda Torque Servo
| 2    | INTAKE_JOINT_2     | GoBilda Torque Servo
| 3    |                    |
| 4    |                    |
| 5    |                    |

### Sensors (I2C)

| I2C Bus | Sensor Name       | Description
|---------|-------------------|------------------------------------
| 0       | GOBILDA_ODOMETRY  | GoBilda Pinpoint Odometry Sensor
| 1       | LEFT_DISTANCE     | Rev 2M Distance Sensor
| 2       | RIGHT_DISTANCE    | Rev 2M Distance Sensor
| 3       | CENTER_DISTANCE   | Rev 2M Distance Sensor

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

| Port | Motor Name         | Description
|------|--------------------|------------------------------------
| 0    | EXTENDING_ARM      | Yellow Jacket Planetary Gear Motor
| 1    | RISER_LEFT         | Yellow Jacket Planetary Gear Motor
| 2    | RISER_RIGHT        | Yellow Jacket Planetary Gear Motor
| 3    |                    |

### Servos

| Port | Servo Name         | Description
|------|--------------------|------------------------------------
| 0    | LIFT_BUCKET                   | BoBilda Speed Servo
| 1    |                    |
| 2    |                    |
| 3    |                    |
| 4    |                    |
| 5    |                    |

### Sensors (I2C)

| I2C Bus | Sensor Name       | Description
|---------|-------------------|------------------------------------
| 0       |                   |
| 1       |                   |
| 2       |                   |
| 3       |                   |

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
