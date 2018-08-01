function copyToClipboard() {
    var text = document.getElementById("clipboard-input");
    text.select();
    document.execCommand("copy");
}