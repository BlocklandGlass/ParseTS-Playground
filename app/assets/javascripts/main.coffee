requirejs ["bootstrap"], ->

codeField = document.getElementById("code")
if codeField != undefined
  requirejs ["codemirror"], (CodeMirror) ->
    CodeMirror.fromTextArea codeField,
      lineNumbers: true
      readOnly: if codeField.readOnly then "nocursor"