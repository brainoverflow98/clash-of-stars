var buffer = argument[0];
var str = "";
var size = buffer_read(buffer,buffer_u8);
for (var i = 1; i < size+1; i += 1)
{
    var ASCII = buffer_read(buffer,buffer_u8);
    var char = ASCIIToChar(ASCII);
    str = string_insert(char,str,i);  
}
return str;
