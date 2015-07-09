#!/usr/bin/env bash

source base.sh

host=$1
port=$2
echo -e "TESTing EventSuite on host: ${GREEN}http://$host:$port${NC}\n"

#POST /events
event_post=`curl -X POST \
     -sL \
     -w "\\n%{http_code}" \
     -H "Content-Type: application/json" \
     -d '{
            "category": "training",
            "title": "introduction to",
            "summary": "this is..",
            "expiration": "2015-05-04T00:00:00.000+08:00",
            "thumbnail": "",
            "presenter": "huiting",
            "presenterIntroduction": "she is....",
            "location": "Shanghai Building 7 Room 3001",
            "startTime": "2015-05-01",
            "endTime": "2015-05-01",
            "costPerUser": "0",
            "seats": "30",
            "deliveryLanguage": "Chinese",
            "description": "this is...",
            "prerequisites": "no",
            "tags": "",
            "attachments": "",
            "status": "start",
            "user": "huiting"
        }' \
     http://$host:$port/events`
event_post_code=`echo -e "$event_post" | sed -n 2p`
event_post_response=`echo -e "$event_post" | sed -n 1p`
check 'POST /events' $event_post_code
echo -e $event_post_response
echo

event_uuid=`uuid "$event_post_response"`
echo -e EVENT UUID: $event_uuid
echo

#POST /comment
comment_event_post=`curl -X POST \
     -sL \
     -w "\\n%{http_code}" \
     -H "Content-Type: application/json" \
     -d "{
            \"eventId\": \"$event_uuid\",
            \"eventTitle\": \"introduction to\",
            \"eventType\": \"event\",
            \"userId\": \"$event_uuid\",
            \"comment\": \"Test comment for event.\"
        }" \
     http://$host:$port/comment`
comment_event_post_code=`echo -e "$comment_event_post" | sed -n 2p`
comment_event_post_response=`echo -e "$comment_event_post" | sed -n 1p`
check 'POST /comment' $comment_event_post_code
echo -e $comment_event_post_response
echo

comment_uuid=`uuid "$comment_event_post_response"`
echo -e COMMENT UUID: $comment_uuid
echo

#DELETE /comment/:commentId
comment_event_delete=`curl -X DELETE \
     -sL \
     -w "\\n%{http_code}" \
     http://$host:$port/comment/$comment_uuid`
comment_event_delete_code=`echo -e "$comment_event_delete" | sed -n 2p`
comment_event_delete_response=`echo -e "$comment_event_delete" | sed -n 1p`
check 'DELETE /comment/:commentId' $comment_event_delete_code
echo -e $comment_event_delete_response
echo

#POST /like/event
like_event_post=`curl -X POST \
     -sL \
     -w "\\n%{http_code}" \
     -H "Content-Type: application/json" \
     -d "{
            \"eventId\": \"$event_uuid\",
            \"likeUserId\": \"bbsmrdj\",
            \"title\": \"introduction to\"
        }" \
     http://$host:$port/like/event`
like_event_post_code=`echo -e "$like_event_post" | sed -n 2p`
like_event_post_response=`echo -e "$like_event_post" | sed -n 1p`
check 'POST /like/event' $like_event_post_code
echo -e $like_event_post_response
echo

#DELETE /like/event/:eventId/:userId
unlike_event_post=`curl -X DELETE \
     -sL \
     -w "\\n%{http_code}" \
     http://$host:$port/like/event/$event_uuid/bbsmrdj`
unlike_event_post_code=`echo -e "$unlike_event_post" | sed -n 2p`
unlike_event_post_response=`echo -e "$unlike_event_post" | sed -n 1p`
check 'DELETE /like/event/:eventId/:userId' $unlike_event_post_code
echo -e $unlike_event_post_response
echo

#POST /follow/event
follow_event_post=`curl -X POST \
     -sL \
     -w "\\n%{http_code}" \
     -H "Content-Type: application/json" \
     -d "{
            \"eventId\": \"$event_uuid\",
            \"follower\": \"bbsmrdj\",
            \"title\": \"introduction to\"
        }" \
     http://$host:$port/follow/event`
follow_event_post_code=`echo -e "$follow_event_post" | sed -n 2p`
follow_event_post_response=`echo -e "$follow_event_post" | sed -n 1p`
check 'POST /follow/event' $follow_event_post_code
echo -e $follow_event_post_response
echo

#DELETE /follow/event/:eventId/:userId
unfollow_event_post=`curl -X DELETE \
     -sL \
     -w "\\n%{http_code}" \
     http://$host:$port/like/event/$event_uuid/bbsmrdj`
unfollow_event_post_code=`echo -e "$unfollow_event_post" | sed -n 2p`
unfollow_event_post_response=`echo -e "$unfollow_event_post" | sed -n 1p`
check 'DELETE /follow/event/:eventId/:userId' $unfollow_event_post_code
echo -e $unfollow_event_post_response
echo

#DELETE /events/:eventId
delete_event_post=`curl -X DELETE \
     -sL \
     -w "\\n%{http_code}" \
     "http://$host:$port/events/$event_uuid?username=admin&password=password"`
delete_event_post_code=`echo -e "$delete_event_post" | sed -n 2p`
delete_event_post_response=`echo -e "$delete_event_post" | sed -n 1p`
check 'DELETE /events/:eventId' $delete_event_post_code
echo -e $delete_event_post_response
echo
