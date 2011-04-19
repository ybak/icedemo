start "IceGridNode config.grid" cmd /k icegridnode --Ice.Config=./src/main/resources/config.grid
ping -n 4 127.0.0.1>nul
start "IceGridNode config.node1" cmd /k icegridnode --Ice.Config=./src/main/resources/config.node1