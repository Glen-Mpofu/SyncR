robocopy "C:\Users\Reception\Documents" "E:\Documents" /MIR /XO /xx /z /copy:DATSO /R:5 /W:1 /MT:32 /DCOPY:DATEX /LOG:C:\Logs\c_robocopy_sync.log

robocopy "E:\Documents" "C:\Users\Reception\Documents" /MIR /XO /xx /z /copy:DATSO /R:5 /W:1 /MT:32 /DCOPY:DATEX /LOG:C:\Logs\e_robocopy_sync.log

robocopy "C:\Users\Reception\Documents" "E:\Documents" /MIR /XO /xx /z /copy:DATSO /R:5 /W:1 /MT:32 /DCOPY:DATEX /LOG:C:\Logs\c_robocopy_sync.log
