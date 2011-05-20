function toggleDiv(divid){
    if(document.getElementById(divid).style.display == 'none'){
      document.getElementById(divid).style.display = 'block';
    }else{
      document.getElementById(divid).style.display = 'none';
    }
}

// usage: <a href="javascript:openAll();">openAll</a>
function openAll( ) {
    var divs = document.getElementsByTagName("div");
    for ( t = 0; t < divs.length; ++t )    {
         var td = divs[t];
         if (td.className == "PAYLOAD"){
             td.style.display = "block";
         }
    }
}

// usage: <a href="javascript:openAll();">closeAll</a>
function closeAll( ){
    var divs = document.getElementsByTagName("div");
    for ( t = 0; t < divs.length; ++t )    {
        var td = divs[t];
        if (td.className == "PAYLOAD"){
            td.style.display = "none";
        }
    }
}