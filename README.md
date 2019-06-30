# SpringBoot Two Way SSL Authentication Example

Simple example with two springboot applications communicating through https with two way SSL authentication using certificate chain.


## Create A Self Signed Certificate Chain

We will use the chain to avoid import every client certificate in the server, so the server can trust in the intermediate certificate instead every client.

### Generate private keys for root and ca (intermediate) - Root -> Intermediate CA -> Client
```sh
keytool -genkeypair -alias root -dname cn=root -validity 10000 -keyalg RSA -keysize 2048 -ext bc:c -keystore root.jks -keypass password -storepass password
keytool -genkeypair -alias ca -dname cn=ca -validity 10000 -keyalg RSA -keysize 2048 -ext bc:c -keystore ca.jks -keypass password -storepass password
```

### Generate root certificate
```sh
keytool -exportcert -rfc -keystore root.jks -alias root -storepass password > root.pem
```

### Generate a certificate for ca signed by root (root -> ca)
```sh
keytool -keystore ca.jks -storepass password -certreq -alias ca | keytool -keystore root.jks -storepass password -gencert -alias root -ext bc=0 -ext san=dns:ca -rfc > ca.pem
```

### Import ca certificate chain into ca.jks
```sh
keytool -keystore ca.jks -storepass password -importcert -trustcacerts -noprompt -alias root -file root.pem
keytool -keystore ca.jks -storepass password -importcert -alias ca -file ca.pem
```

### Generate a certificate for client-rest signed by ca (root -> ca -> client-rest)
```sh
keytool -keystore client-rest.jks -storepass client-rest -certreq -alias client-rest | keytool -keystore ca.jks -storepass password -gencert -alias ca -ext ku:c=dig,keyEnc -ext SAN=dns:localhost,ip:127.0.0.1 -ext eku=sa,ca -rfc > client-rest.pem
```

### Import client-rest certificate chain into client-rest.jks
```sh
keytool -keystore client-rest.jks -storepass client-rest -importcert -trustcacerts -noprompt -alias root -file root.pem
keytool -keystore client-rest.jks -storepass client-rest -importcert -alias ca -file ca.pem
keytool -keystore client-rest.jks -storepass client-rest -importcert -alias client-rest -file client-rest.pem
```

### Create Self Signed server-rest Certificate
```sh
keytool -genkeypair -alias client-rest -keyalg RSA -keysize 2048 -storetype JKS -keystore client-rest.jks -validity 3650 -ext SAN=dns:localhost,ip:127.0.0.1
```

## Setup Trust between Applications

To setup the trust, we need to import the intermediate client certificate in to the server's trusted certificates and the server-rest certificate in the client-rest jks:

### Export server-rest public certificate
```sh
keytool -export -alias client-rest -file client-rest.crt -keystore client-rest.jks
```

### Import intermediate client-rest public certificate to server-rest jks (ca.pem already created)
```sh
keytool -import -alias ca -file ca.pem -keystore server-rest.jks
```

### Import server-rest public certificate to client-rest jks
```sh
keytool -import -alias client-rest -file client-rest.crt -keystore server-rest.jks
```


## Keystores Final Result

### keytool -list -keystore server-rest.jks 

```sh
ca, 29/06/2019, trustedCertEntry, 
Fingerprint (SHA1) do certificado: 35:F6:DF:85:6D:56:46:5F:3E:9C:A2:F6:D5:87:62:15:AA:6C:23:95
server-rest, 29/06/2019, PrivateKeyEntry, 
Fingerprint (SHA1) do certificado: C5:C4:F6:79:BC:DD:BB:D6:05:8F:6A:C8:FA:7A:89:5D:BC:3D:3B:04
```


### keytool -list -keystore client-rest.jks

```sh
root, 29/06/2019, trustedCertEntry, 
Fingerprint (SHA1) do certificado: 7C:1D:3F:15:3B:00:8C:F9:B5:54:25:32:90:B6:BE:77:86:28:92:0A
ca, 29/06/2019, trustedCertEntry, 
Fingerprint (SHA1) do certificado: 35:F6:DF:85:6D:56:46:5F:3E:9C:A2:F6:D5:87:62:15:AA:6C:23:95
client-rest, 29/06/2019, PrivateKeyEntry, 
Fingerprint (SHA1) do certificado: BA:73:7F:AC:CB:D8:01:FA:40:12:14:2E:23:0E:58:58:B7:D4:8E:72
server-rest, 29/06/2019, trustedCertEntry, 
Fingerprint (SHA1) do certificado: C5:C4:F6:79:BC:DD:BB:D6:05:8F:6A:C8:FA:7A:89:5D:BC:3D:3B:04
```


## Export the client-rest certificate to p12 format, and import in the browser to use the services
```sh
keytool -importkeystore -srckeystore client-rest.jks -destkeystore client-rest.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass client-rest -deststorepass client-rest -srcalias client-rest -destalias client-rest -srckeypass client-rest -destkeypass client-rest -noprompt
```

PS. We have to import the keystore because of the two way ssl, the browser becomes a client and need to be authenticated.


## Running the example

* Start server-rest running ServerApplication class
* Start client-rest with ClientApplication class
* Import the certificates in the browser settings (client-rest.p12)
* Access https://localhost:9443/client-rest/infos and https://localhost:8443/server-rest/infos

