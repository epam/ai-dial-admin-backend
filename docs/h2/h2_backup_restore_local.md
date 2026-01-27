
### H2 Database Backup and Restore Process

#### Backup Process

1. **Downscale MCP Manager Backend**: Set the MCP manager backend replicas to 0 to ensure no new data is written during the backup process.

2. **Create a Simple Deployment**: Use a temporary deployment to access the database files.

   ```yaml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: nginx-deployment
     namespace: dial-admin
     labels:
       app: nginx
   spec:
     replicas: 1
     selector:
       matchLabels:
         app: nginx
     template:
       metadata:
         labels:
           app: nginx
       spec:
         volumes:
           - name: test-admin-db
             persistentVolumeClaim:
               claimName: test-admin-deployment-db
         containers:
         - name: nginx
           image: nginx:1.14.2
           ports:
           - containerPort: 80
           volumeMounts:
               - name: test-admin-db
                 mountPath: /app/data/db
   ```

3. **Access the Pod**: Execute into the pod to manage files.

   ```bash
   kubectl exec -i -t -n dial-admin <nginx-deployment-poduid> -c nginx -- sh -c "clear; (bash || ash || sh)"
   ```

4. **Archive the Database Folder**: Use the following command to archive the database:

   ```bash
   tar cf test.tar app/data/db
   ```

5. **Copy Archive to Local Machine**: Use the command:

   ```bash
   kubectl cp <nginx-deployment-poduid>:test.tar ./test.tar -n dial-admin
   ```

#### Restore Process

1. **Upload and Rename Archive**:  Upload the archive to the file system with the new name `deployment_manager_db.mv.db`:

   ```bash
   kubectl cp ./test.tar <ai-dial-mcp-manager-backend-pod>:../tmp -n dial-admin
   ```

2. **Extract and Move Database**: Execute inside the pod to extract and move the database file.

   ```bash
   kubectl exec -i -t -n dial-admin <ai-dial-mcp-manager-backend-pod> -- sh -c "cd /tmp && tar -xvf test.tar && mv app/data/db/deployment_manager_db.mv.db app/data/db/deployment_manager_db_restore.mv.db"
   ```

3. **Update Database Configuration**: Update the `H2_FILE` variable in your application configuration to point to the new database file:

   ```yaml
   H2_FILE: ./data/db/deployment_manager_db_restore
   ```

4. **Switch MCP Manager to New Database**: Ensure that the application is configured to use the new database file.


**Note**: If using Azure Blob Storage, rename the file inside the blob storage and update the `H2_FILE` variable accordingly. Make sure that blob versioning is enabled

### Important Considerations

- **Ensure Data Consistency**: Downscale the MCP manager backend to prevent data changes during the backup process.
- **Secure Data Transfer**: Use secure methods to transfer the database archive to and from the Kubernetes cluster.
- **Verify Configuration**: Double-check the `H2_FILE` configuration to ensure the application points to the correct database file.
