// MongoDB initialization script

// Connect to admin database
db = db.getSiblingDB('admin');

// Create admin user if it doesn't exist
if (db.getUser('admin') == null) {
    db.createUser({
        user: 'admin',
        pwd: 'admin',
        roles: [{ role: 'userAdminAnyDatabase', db: 'admin' }]
    });
    print('Admin user created successfully');
}

// Switch to application database
db = db.getSiblingDB('automation_framework');

// Create application user if it doesn't exist
if (db.getUser('admin') == null) {
    db.createUser({
        user: 'admin',
        pwd: 'admin',
        roles: [{ role: 'readWrite', db: 'automation_framework' }]
    });
    print('Application user created successfully');
}

// Create collections if they don't exist
db.createCollection('tests');
db.createCollection('test_results');
db.createCollection('logs');
db.createCollection('agents');

// Create indexes
db.tests.createIndex({ 'status': 1 });
db.tests.createIndex({ 'agentId': 1 });
db.test_results.createIndex({ 'testId': 1 });
db.test_results.createIndex({ 'agentId': 1 });
db.logs.createIndex({ 'testId': 1 });
db.logs.createIndex({ 'agentId': 1 });
db.logs.createIndex({ 'timestamp': 1 });

print('Collections and indexes created successfully');