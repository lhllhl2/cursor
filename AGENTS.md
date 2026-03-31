## Cursor Cloud specific instructions

### Oracle XE 21c (Docker)

数据库通过 Docker 容器运行，配置见 `docker-compose.yml`。

**启动方式：**
```bash
sudo dockerd > /tmp/dockerd.log 2>&1 &   # 如果 Docker daemon 未运行
sudo docker compose up -d                  # 启动 Oracle XE
```

**连接信息：**

| 参数 | 值 |
|------|-----|
| Host | `localhost` |
| Port | `1521` |
| SID (CDB) | `XE` |
| PDB Service | `XEPDB1` |
| SYS/SYSTEM 密码 | `oracle123` |
| 开发用户 | `devuser` / `devuser123` |

**连接字符串示例：**
- JDBC: `jdbc:oracle:thin:@localhost:1521/XEPDB1`
- SQL*Plus (容器内): `sqlplus devuser/devuser123@localhost:1521/XEPDB1`

**注意事项：**
- Oracle XE 首次启动初始化约需 5-10 分钟，等待日志出现 `DATABASE IS READY TO USE!`。
- 此环境为 Docker-in-Docker，需要 `fuse-overlayfs` 存储驱动和 `iptables-legacy`。
- Docker daemon 需要手动启动（`sudo dockerd`），不会自动随系统启动。
