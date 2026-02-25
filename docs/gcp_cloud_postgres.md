
# GCP Database Setup & Connection Using Workload Identity

## Prerequisites

- Google Cloud account with billing enabled
- gcloud CLI installed and authenticated
- GKE cluster with Workload Identity enabled
- kubectl configured for your cluster

---

## 1. Create a Cloud SQL Database

1. **Enable APIs:**
   ```sh
   gcloud services enable sqladmin.googleapis.com
   ```

2. **Create a Cloud SQL Instance:**
   ```sh
   gcloud sql instances create my-postgres-instance \
     --database-version=POSTGRES_13 \
     --cpu=1 --memory=4GB --region=us-central1
   ```

3. **Set a password for the default user:**
   ```sh
   gcloud sql users set-password postgres \
     --instance=my-postgres-instance \
     --password=YOUR_PASSWORD
   ```

4. **Create a database:**
   ```sh
   gcloud sql databases create mydatabase --instance=my-postgres-instance
   ```

---

## 2. Set Up Workload Identity

1. **Create a Service Account:**
   ```sh
   gcloud iam service-accounts create my-db-sa \
     --display-name="Cloud SQL Workload Identity"
   ```

2. **Grant Cloud SQL Client Role:**
   ```sh
   gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
     --member="serviceAccount:my-db-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
     --role="roles/cloudsql.client"
   ```

3. **Annotate Kubernetes Service Account:**
   ```sh
   kubectl create serviceaccount k8s-db-sa
   kubectl annotate serviceaccount k8s-db-sa \
     iam.gke.io/gcp-service-account=my-db-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com
   ```

4. **Allow Kubernetes Service Account to Impersonate GCP Service Account:**
   ```sh
   gcloud iam service-accounts add-iam-policy-binding my-db-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com \
     --role roles/iam.workloadIdentityUser \
     --member "serviceAccount:YOUR_PROJECT_ID.svc.id.goog[default/k8s-db-sa]"
   ```

---

## 3. Change Database Ownership to Workload Identity User

### 4.1. Enable IAM Database Authentication

1. **Enable IAM authentication on your Cloud SQL instance:**
   ```sh
   gcloud sql instances patch my-postgres-instance --enable-iam-authentication
   ```

### 4.2. Create an IAM Database User

Cloud SQL automatically allows IAM users to connect if they have the correct permissions. The database user name must match the IAM principal (email address) of the service account.

1. **Get the GCP Service Account email:**
   ```
   my-db-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com
   ```

2. **Connect to your database as a superuser (e.g., `postgres`):**
   ```sh
   gcloud sql connect my-postgres-instance --user=postgres
   ```

3. **Create a database user that matches the service account email:**
   ```sql
   CREATE ROLE "my-db-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com" LOGIN;
   ```

   > Note: For IAM authentication, you do not set a password.

### 4.3. Change Database Ownership

1. **Change the owner of the database:**
   ```sql
   ALTER DATABASE mydatabase OWNER TO "my-db-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com";
   ```

2. **(Optional) Change ownership of existing tables and objects:**
   ```sql
   -- For each table:
   ALTER TABLE mytable OWNER TO "my-db-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com";
   -- For schemas, sequences, etc., repeat as needed.
   ```

---

## 5.5. Update `values.yaml` (`backend.envs` Section)

Add or update the following entries in your `values.yaml` under the `backend.envs` section:

```yaml
backend:
  envs:
    DATASOURCE_AUTH_TYPE: "gcp"
    POSTGRES_DATASOURCE_URL: "jdbc:postgresql:///db-name?cloudSqlInstance=project-name:region:instance-id&enableIamAuth=true"
    POSTGRES_DATASOURCE_USERNAME: "my-db-sa@project-name.iam.gserviceaccount.com"
```

**Replace the following placeholders:**
- `db-name` with your actual database name.
- `project-name` with your GCP project ID.
- `region` with your Cloud SQL instance region.
- `instance-id` with your Cloud SQL instance ID.
- `my-db-sa@project-name.iam.gserviceaccount.com` with your actual service account email.

---

## References

- [Cloud SQL IAM Database Authentication](https://cloud.google.com/sql/docs/postgres/authentication)
- [Workload Identity for GKE](https://cloud.google.com/kubernetes-engine/docs/how-to/workload-identity)
- [Change PostgreSQL Database Owner](https://www.postgresql.org/docs/current/sql-alterdatabase.html)