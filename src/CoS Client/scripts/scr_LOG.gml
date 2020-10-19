var message=argument[0]
var colour=argument[1]
var OK=false
for (var i=1 ; i<6 ;i++;)
{
    if obj_LOG.line[i]=""
    {
        obj_LOG.line[i]=message
        obj_LOG.clr[i]=colour
        OK=true
        break;
    }
}
if OK=false
{
    for (var i=1 ; i<5 ;i++;)
    {
        obj_LOG.line[i]=string(obj_LOG.line[i+1])
        obj_LOG.clr[i]=obj_LOG.clr[i+1]                
    }
    obj_LOG.line[5]=message
    obj_LOG.clr[5]=colour    
}
obj_LOG.alpha = 1
