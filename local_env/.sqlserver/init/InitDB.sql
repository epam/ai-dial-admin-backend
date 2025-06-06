USE [master];
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'testdb')
BEGIN
    CREATE DATABASE [testdb] COLLATE SQL_Latin1_General_CP1_CS_AS;
END