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

### Kubernetes (k3s)

使用 k3s 作为轻量 K8s 发行版，因嵌套容器环境 cgroup v2 限制，以 control-plane-only 模式运行（`--disable-agent`）。

**启动方式：**
```bash
sudo k3s server --disable-agent --write-kubeconfig-mode=644 > /tmp/k3s.log 2>&1 &
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
```

**可用组件：**
- kubectl v1.35.3
- k3s v1.34.6+k3s1（API server、scheduler、controller-manager）
- kind v0.27.0、k3d v5.8.3、minikube v1.38.1（已安装但因 cgroup 限制无法在此环境中创建完整集群）

**功能范围：**
- K8s API 完全可用：namespace、configmap、deployment、service、secret 等资源的 CRUD
- `kubectl apply`、`kubectl create`、dry-run 等均正常
- 无 kubelet/节点，Pod 不会实际调度运行

**注意事项：**
- 此嵌套容器环境 cgroup v2 为 threaded mode，无法启用 memory controller，导致 kubelet 无法运行
- 如需运行 Pod，需在有完整 cgroup 支持的环境（物理机/VM）中使用
- `KUBECONFIG` 环境变量必须设置为 `/etc/rancher/k3s/k3s.yaml`
