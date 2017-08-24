Set WshShell = WScript.CreateObject("WScript.Shell")
Set locator = CreateObject("WbemScripting.SWbemLocator")
Set service = locator.ConnectServer()
Set processes = service.ExecQuery("select Name, CommandLine, ProcessId from Win32_Process")
wscript.echo "---"
For Each process in processes
    wscript.echo process.Name & "|" & process.CommandLine & "|" & process.ProcessId
Next
wscript.echo "---"
Set WSHShell = Nothing