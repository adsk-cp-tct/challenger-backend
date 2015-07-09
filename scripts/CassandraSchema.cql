/**
 * DELETE OLD DATA
 */
DROP KEYSPACE challenger;

/**
 * CREATE AND USE NEW KEYSPACE [challenger]
 */
CREATE KEYSPACE challenger WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
USE challenger;

/**
 * TABLE Event
 */
CREATE TABLE Event(
    id  timeuuid,
    category text,
    title text,
    summary text,
    expiration timestamp,
    thumbnail text,
    location text,
    startTime timestamp,
    endTime timestamp,
    costPerSeat text,
    seats text,
    deliveryLanguage text,
    description text,
    status text,
    createdTime timestamp,
    createdBy text,
    updatedTime timestamp,
    updatedBy text,
    partition text,
    PRIMARY KEY(id)
);

CREATE INDEX Event_CreatedTime on Event (createdTime);
CREATE INDEX Event_Category on Event (category);

/**
 * TABLE EventExtension
 */
CREATE TABLE EventExtension(
    id  timeuuid,
    presenter text,
    presenterLogo text,
    presenterEmail text,
    presenterTitle text,
    prerequisites text,
    tags text,
    attachments text,
    registerPolicy text,
    likeUsers Set<text>,
    registeredUsers Map<text, timestamp>,
    applyingUsers Map<text, timestamp>,
    followers Set<text>,
    PRIMARY KEY(id)
);

/**
 * TABLE User
 */
CREATE TABLE User(
    id      timeuuid,
    groupMembers    Set<text>,
    nickName    text,
    realName    text,
    email       text,
    password    text,
    avatar      text,
    description text,
    followers   Set<text>,
    followUsers  Set<text>,
    registeredEvents    Set<text>,
    applyingEvents    Set<text>,
    createdTime timestamp,
    updatedTime timestamp,
    lastLoginTime   timestamp,
    PRIMARY KEY(id)
);

CREATE INDEX User_email on User (email);
CREATE INDEX User_password on User (password);

/**
 * TABLE Activity
 */
CREATE TABLE Activity(
    id  timeuuid,
    subject text,
    subjectId timeuuid,
    verb    text,
    objectId timeuuid,
    obj  text,
    objType  text,
    data    text,
    createdTime timestamp,
    creator text,
    PRIMARY KEY(id)
);

/**
 * TABLE Comment
 */
CREATE TABLE Comment(
    id  timeuuid,
    eventId timeuuid,
    userId  timeuuid,
    content text,
    createdTime timestamp,
    PRIMARY KEY(eventId, id)
);

CREATE INDEX Comment_id on Comment (id);

/**
 * TABLE Idea
 */
CREATE TABLE Idea(
    id  timeuuid,
    title text,
    description text,
    thumbnails Set<text>,
    followers Set<timeuuid>,
    likedUsers Set<timeuuid>,
    createdTime timestamp,
    createdBy timeuuid,
    partition text,
    PRIMARY KEY(id)
);

CREATE INDEX Idea_CreatedTime on Idea (createdTime);
CREATE INDEX Idea_CreatedBy on Idea (createdBy);
