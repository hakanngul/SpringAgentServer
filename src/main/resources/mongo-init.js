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
db.createCollection('test_suites');
db.createCollection('test_suite_results');

// Create indexes for tests
db.tests.createIndex({ 'status': 1 });
db.tests.createIndex({ 'agentId': 1 });
db.tests.createIndex({ 'createdAt': 1 });
db.tests.createIndex({ 'tags': 1 });

// Create indexes for test results
db.test_results.createIndex({ 'testId': 1 });
db.test_results.createIndex({ 'agentId': 1 });
db.test_results.createIndex({ 'createdAt': 1 });

// Create indexes for logs
db.logs.createIndex({ 'testId': 1 });
db.logs.createIndex({ 'agentId': 1 });
db.logs.createIndex({ 'timestamp': 1 });
db.logs.createIndex({ 'level': 1 });

// Create indexes for agents
db.agents.createIndex({ 'status': 1 });
db.agents.createIndex({ 'lastActivity': 1 });

// Create indexes for test suites
db.test_suites.createIndex({ 'status': 1 });
db.test_suites.createIndex({ 'createdAt': 1 });
db.test_suites.createIndex({ 'tags': 1 });

// Create indexes for test suite results
db.test_suite_results.createIndex({ 'suiteId': 1 });
db.test_suite_results.createIndex({ 'status': 1 });
db.test_suite_results.createIndex({ 'createdAt': 1 });
db.test_suite_results.createIndex({ 'agentId': 1 });

print('Collections and indexes created successfully');