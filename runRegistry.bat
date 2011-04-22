start "IceGridNode config.grid" cmd /k icegridnode --Ice.Config=./src/main/resources/config.grid
ping -n 4 127.0.0.1>nul
start "IceGridNode config.node1" cmd /k icegridnode --Ice.Config=./src/main/resources/config.node1

icegridadmin --Ice.Config=src\main\resources\config.grid -e "application remove Simple"
icegridadmin --Ice.Config=src\main\resources\config.grid -e "application add 'src\main\resources\application.xml'"

start "glacier2router" cmd /k glacier2router --Ice.Config=src/main/resources/config.glacier2

java -jar %ICE_HOME%\bin\IceGridGUI.jar

