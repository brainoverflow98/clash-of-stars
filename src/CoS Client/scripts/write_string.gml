var buffer = argument[0];
var text = argument[1];
buffer_write(buffer,buffer_u8,string_length(text));
for (var i = 1; i < string_length(text)+1; i += 1)
{
    var char = string_char_at(text,i);
    var ASCII = charToASCII(char);
    buffer_write(buffer,buffer_u8,ASCII);
}
