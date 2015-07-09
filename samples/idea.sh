#!/usr/bin/env bash

source base.sh

host=$1
port=$2
echo -e "TESTing IdeaSuite on host: ${GREEN}http://$host:$port${NC}\n"

#POST /idea
idea_post=`curl -X POST \
     -sL \
     -w "\\n%{http_code}" \
     -H "Content-Type: application/json" \
     -d '{
            "title":"An Idea",
            "description":"An idea description",
            "thumbnails":["thumb1", "thumb2"],
            "followers": ["bbsmrdj", "flora"],
            "likedUsers": ["bbsmrdj"],
            "createdBy": "bbsmrdj"
        }' \
     http://$host:$port/idea`
idea_post_code=`echo -e "$idea_post" | sed -n 2p`
idea_post_response=`echo -e "$idea_post" | sed -n 1p`
check 'POST /idea' $idea_post_code
echo -e $idea_post_response
echo

idea_uuid=`uuid "$idea_post_response"`
echo IDEA UUID: $idea_uuid
echo

#GET /idea/:idea_uuid
idea_get=`curl -X GET \
     -sL \
     -w "\\n%{http_code}" \
     http://$host:$port/idea/$idea_uuid`
idea_get_code=`echo -e "$idea_get" | sed -n 2p`
idea_get_response=`echo -e "$idea_get" | sed -n 1p`
check 'GET /idea' $idea_get_code
echo -e $idea_get_response
echo

#GET /idea
idea_get_list=`curl -X GET \
     -sL \
     -w "\\n%{http_code}" \
     http://$host:$port/idea`
idea_get_list_code=`echo -e "$idea_get_list" | sed -n 2p`
idea_get_list_response=`echo -e "$idea_get_list" | sed -n 1p`
check 'GET /idea' $idea_get_list_code
echo -e $idea_get_list_response
echo

