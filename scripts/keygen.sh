keytool -keystore .api_key_store -genkey -keyalg rsa -alias api_key -dname "OU=organizations, \
O=scalcube.io" -storepass YqeRYSc3ybV4?@nV -keypass YqeRYSc3ybV4?@nV -storetype pkcs12

keytool -keystore .api_key_store -certreq -alias api_key -keyalg rsa -storepass YqeRYSc3ybV4?@nV \
-file api_key.csr

keytool -keystore .api_key_store -alias api_key -gencert -infile api_key.csr -outfile api_key.cer \
-dname "OU=organizations, O=scalcube.io" -validity 365 -storepass YqeRYSc3ybV4?@nV

keytool -noprompt -importcert -file api_key.cer -alias api_key -storepass YqeRYSc3ybV4?@nV \
-alias api_key -keystore .api_key_trust_store -storetype pkcs12
