strComputer = "."
Set objWMIService = GetObject("winmgmts:{impersonationLevel=impersonate}!\\" & strComputer & "\root\cimv2")
Set colDisks = objWMIService.ExecQuery ("Select DeviceID,DriveType from Win32_LogicalDisk where DeviceID = '{drive}'")
For Each objDisk in colDisks    
	 Select Case objDisk.DriveType
        Case 1
            driveType = "err"
        Case 2
            driveType = "removable"
        Case 3
            driveType = "local"
        Case 4
				driveType = "network"      
        Case 5
				driveType = "compact"      
        Case 6
				driveType = "ram"   
        Case Else
            driveType = "err_else"
    End Select
    Wscript.Echo "DeviceID=" & objDisk.DeviceID & "|DriveType=" & driveType
Next