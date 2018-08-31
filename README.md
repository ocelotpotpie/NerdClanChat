# NerdClanChat

A Java reimplementation of [CHClanChat](https://github.com/NerdNu/CHClanChat). The basics should be functionally identical to the user, but perform better on the backend. Some UI things have been cleaned up, and new features added.


## Build Instructions

 1. Build [BukkitEBean](https://github.com/NerdNu/BukkitEBean) and install in the local Maven repository.
   ```
git clone https://github.com/NerdNu/BukkitEBean
cd BukkitEBean
mvn clean install
   ```
 2. Build NerdClanChat:
   ```
cd ..
git clone https://github.com/NerdNu/NerdClanChat
cd NerdClanChat
mvn clean package
   ```

