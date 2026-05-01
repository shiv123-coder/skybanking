# 🚀 Quick Start Guide

> [!NOTE]
> All detailed documentation has been consolidated into the [README.md](./README.md).

## ⚡ Essential Commands

### 🗄️ Database Setup
```bash
psql -U postgres -d skybank -f database/skybanking_schema_pg.sql
```

### 🔨 Build & Package
```bash
mvn clean package
```

### 🚢 Deploy to Tomcat
```bash
copy target\BankingWebApp.war C:\apache-tomcat-10.1.43\webapps\
```

---
[Return to Main Documentation](./README.md)
