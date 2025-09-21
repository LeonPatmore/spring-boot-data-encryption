db = db.getSiblingDB('myappdb');
db.createUser({
  user: "myappuser",
  pwd: "mypassword",
  roles: [{ role: "readWrite", db: "myappdb" }]
});
