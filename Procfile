web: target/start -Dhttp.port=${PORT} ${JAVA_OPTS} -DapplyEvolutions.default=true -Ddb.default.driver=org.postgresql.Driver -Ddb.default.url=${DATABASE_URL} -Dapplication.secret=${APP_SECRET} -Dsmtp.host=smtp.sendgrid.net -Dsmtp.port=587 -Dsmtp.ssl=yes -Dsmtp.user=$SENDGRID_USERNAME -Dsmtp.password=$SENDGRID_PASSWORD -Dopenid.securecallback=$OPENID_SECURE_CALLBACK

