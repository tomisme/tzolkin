var r = require('rethinkdbdash')();

const TEST_USERS = [
  {
    "username": "tom",
    "email": "tzolkin@tomisme.com"
  },
  {
    "username": "elisa",
    "email": "elisa@tomisme.com"
  }
];

r.table('users').insert(TEST_USERS);
