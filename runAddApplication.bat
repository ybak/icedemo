title IceGridAdmin
icegridadmin --Ice.Config=src\main\resources\config.grid -e "application remove Simple"
cmd /k icegridadmin --Ice.Config=src\main\resources\config.grid -e "application add 'src\main\resources\application.xml'"