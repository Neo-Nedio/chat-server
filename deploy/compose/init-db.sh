#!/bin/sh
set -e

mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" \
  < /docker-entrypoint-initdb.d/chat.sql.template
