
#SET COLOR
NC='\033[0m'
RED='\033[0;31m'
GREEN='\033[0;32m'

#PRINT RESULT
function check {
  if [ $2 -eq 200 ]
    then
      echo -e "$1 - ${GREEN}$2${NC}"
    else
      echo -e "$1 - ${RED}$2${NC}"
  fi
}

#GREP UUID, e.g. 0207cc80-fa0a-11e4-b6db-fb1d71449131
uuid_reg_exp='[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}'
function uuid {
  local res=`echo -e $1 | grep -oEi $uuid_reg_exp | head -n 1`
  echo "$res"
}

