To produce the auths content values, you needs to base64 encode the credentials. 

This means, for example that if you want to use:

 Login:    admin@core.collectionspace.org
 Password: Administrator

Then you have to encode the string "admin@core.collectionspace.org:Administrator" which
should yield the encoded string: "YWRtaW5AY29yZS5jb2xsZWN0aW9uc3BhY2Uub3JnOkFkbWluaXN0cmF0b3I="

A convenient way to encode and decode (to verify results, or check values that
are already coded in the various xmlReplay files) is to use the online service 
at: http://www.motobit.com/util/base64-decoder-encoder.asp