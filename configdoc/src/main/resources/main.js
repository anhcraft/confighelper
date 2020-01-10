const elem = document.getElementById("toggle-example");
elem.onclick = function(){
    Array.from(document.getElementsByClassName("examples")).forEach(function(e){
        const isShowing = e.style.display === "block";
        e.style.display = isShowing ? "none" : "block";
        window.localStorage.setItem("configdoc", isShowing ? 0 : 1);
    });
};

const confDoc = window.localStorage.getItem("configdoc");
if(confDoc === "1"){
    elem.click();
}
