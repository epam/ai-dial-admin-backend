### Build aidial/ai-dial-admin-backend:latest image
```shell
docker build -t aidial/ai-dial-admin-backend:latest ../.
```

### Start local env
```shell
docker-compose up
```

### Stop local env
```shell
docker-compose down
```

### Generate H2 credentials
use com.epam.aidial.cfg.encryption.GenerateSecretsTest to create a new pair of Master key and data encryption key.