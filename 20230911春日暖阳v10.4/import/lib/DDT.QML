[General]
SyntaxVersion=2
MacroID=98c3354a-ba96-4921-82dc-db73a0e06197
[Comment]

[Script]

/**
 * ---------------------------文件区-------------------------------
 */

// 保存所有打手的句柄，在运行挂机脚本时排除这些句柄
Sub saveMaster(hwnd)
	If debug Then 
		TracePrint hwnd
	End If
	
	Dim hwndFile
	hwndFile = "C:/tmp/hwnds.txt"
	If Plugin.File.IsFileExist(hwndFile) = False Then 
		Plugin.File.WriteFileEx hwndFile, hwnd
	ElseIf Len(Lib.文件.读取指定行文本内容(hwndFile, 0)) = 0 Then
		// 文件存在但内容为空
		Call Lib.文件.替换指定行文本内容(hwndFile, hwnd, 0)
	Else 
		// 读取原内容，并将,句柄加在后面
		arr = strToArray(Lib.文件.读取指定行文本内容(hwndFile, 0))
		If UBOUND(arr) = - 1  Then 
			Call Lib.文件.替换指定行文本内容(hwndFile, hwnd, 0)
		Else 
			Dim repeat, resultStr
			repeat = false
			
			For i = 0 To UBOUND(arr)
				// 句柄存在
				If Plugin.Window.IsWindow(arr(i)) = 1 Then 
					resultStr = resultStr & arr(i) & ","
				End If
				If (arr(i) + 0) = hwnd Then 
					repeat = true
				End If
			Next
			If repeat = false Then 
				resultStr = resultStr & hwnd & ","
			End If
			If Len(resultStr) <> 0 Then 
				resultStr = Left(resultStr, Len(resultStr) - 1)
			End If
			If debug Then 
				TracePrint resultStr
			End If
			Call Lib.文件.替换指定行文本内容(hwndFile,resultStr,0)
		End If
	End If
End Sub

Function strToArray(str)
	strToArray = Split(str, ",")
End Function

/**
 * --------------------------主函数区-------------------------------
 **/
 
Function offsetRepair
	Plugin.Window.SetClientSize hwnd, 1200, 900
	sRect = Plugin.Window.GetWindowRect(hwnd)
	TracePrint sRect
	sRect = Split(sRect, "|")
	W1 = Clng(sRect(0)) : H1 = Clng(sRect(1))
	W2 = Clng(sRect(2)) : H2 = Clng(sRect(3))
	If FindPic(W1, H1, W2, H2, "C:\tmp\矫正标识.bmp", 0.8, intX, intY) > - 1  Then 
		TracePrint intX
		dx = intX - 385 - W1
		dy = intY - 575 - H1
		offsetRepair = true
	Else 
		offsetRepair = false
		MsgBox "偏移矫正失败，请将窗口激活并处于房间内！", 48, "警告"
		ExitScript
	End If
End Function


// 根据敌我的位置获取最佳角度
// vertical: 正数表示比敌人高，负数表示比敌人低，单位：像素
// horizontal：必须是正数，单位：像素
// unitDistance：每1距的长度，单位：像素
Function getBestAngle(vertical, horizontal, unitDistance)
	Dim theta
	theta = - Atn(vertical / horizontal)  * 180 / (4 * Atn(1))
 	TracePrint "当前夹角：" & theta
	// 如果敌人在下面，那么直接20度或30度就可以
	If theta < -10 Then 
		getBestAngle = 30
		Exit Function
	ElseIf theta < 0 Then
		getBestAngle = 20
		Exit Function
	End If
	vertical = Abs(vertical) / unitDistance
	// 以夹角为界限，低于某个值的直接使用30度，其余情况的视垂直偏差情况使用50、65、70度
	If theta <= 20 Then 
		getBestAngle = 30
	ElseIf theta <= 26 Then
		getBestAngle = 40
	ElseIf theta <= 32 Then
		getBestAngle = 45
	ElseIf theta <= 50 Then
		getBestAngle = 50
	ElseIf theta <= 65 Then
		getBestAngle = 65
	Else 
		getBestAngle = 70
	End If
	// TODO 再检测垂直距离，如果垂直距离过高，则提高角度，ddtank中1屏高6距，超过半屏就该
End Function

// 根据角度、水平距离、垂直距离、1距的距离来就按力度，并对角度进行修正
// 水平距离：永远大于0，垂直距离：如果是正数表示Boss在自己的下面，否则Boss在上面
Function getStrength(angle, horizontal, vertical)
	TracePrint "垂直距离：" & vertical
	TracePrint "水平距离" & horizontal
	// 30、40、45、50、65均已手测
	Dim strengthTable20, strengthTable30, strengthTable35, strengthTable40, strengthTable45, strengthTable50, strengthTable65, strengthTable70
	strengthTable20 = array(10, 19, 25, 30, 36, 40, 44, 48, 51, 54, 57, 60, 63, 66, 69, 72, 74, 76, 78, 80)
	strengthTable30 = array(14, 20, 24, 28, 32, 35, 38, 41, 44, 47, 50, 52, 55, 57, 60, 62, 65, 67, 69, 72)
	strengthTable35 = array(10, 16, 22, 26, 30, 33, 37, 40, 43, 45, 49, 51, 53, 55, 57, 59, 61, 63, 65, 67)
	strengthTable40 = array(12, 16, 20, 25, 30, 33, 35, 40, 43, 45, 48, 50, 53, 55, 58, 60, 62, 64, 67, 70)
	strengthTable45 = array(13, 16, 20, 25, 30, 33, 35, 38, 41, 45, 48, 51, 53, 55, 57, 59, 60, 63, 66, 68)
	strengthTable50 = array(14, 20, 24, 28, 32, 35, 38, 42, 44, 48, 50, 53, 55, 58, 60, 63, 65, 68, 70, 72)
	strengthTable65 = array(13, 20, 26, 31, 37, 41, 44, 48, 53, 56, 58, 61, 64, 67, 70, 73, 76, 79, 82, 85)
	strengthTable70 = array(18.5, 26.4, 32.6, 37.9, 42.7, 47.2, 51.3, 55.3, 59.1, 62.8, 66.3, 69.8, 73.1, 76.5, 79.7, 82.9, 86.1, 89.2, 92.3, 95.3)
	upOffset = Array(3, 2.425, 2, 1.55, 1.9, 1.15, 0.58, 0.24)
	Dim strength
	strength = 0
	If angle <= 20 Then 
		angle = 20
		strength = strengthTable20(Fix(horizontal + 0.000001) + 1)
	ElseIf angle <= 30 Then
		angle = 30
		strength = strengthTable30(Fix(horizontal + 0.000001) + 1)
	ElseIf angle <= 35 Then
		angle = 35
		strength = strengthTable35(Fix(horizontal + 0.000001) + 1)
	ElseIf angle <= 40 Then
		angle = 40
		strength = strengthTable40(Fix(horizontal + 0.000001) + 1)
	ElseIf angle <= 45 Then
		angle = 45
		strength = strengthTable45(Fix(horizontal + 0.000001) + 1)
	ElseIf angle <= 50 Then
		angle = 50
		strength = strengthTable50(Fix(horizontal + 0.000001) + 1)
	ElseIf angle <= 65 Then
		angle = 65
		strength = strengthTable65(Fix(horizontal + 0.000001) + 1)
	Else
		angle = 70
		strength = strengthTable70(Fix(horizontal + 0.000001) + 1)
	End If
	
	If vertical > 2 Then 
		Dim close
		close = 1 / (horizontal / 100)
		close = close / 10
		close = close * close + 1

		Select Case angle
			// 当水平距离越近时，垂直距离越远，力度越大
			// 当水平距离越远时，垂直距离越远，力度增大的幅度变小
			Case 20
				strength = strength + upOffset(0) * vertical * close
			Case 30
				strength = strength + upOffset(1) * vertical * close
			Case 35
				strength = strength + upOffset(2) * vertical * close
			Case 40
				strength = strength + upOffset(3) * vertical * close
			Case 45
				strength = strength + upOffset(4) * vertical * close
			Case 50
				If close >= 5 Then 
					close = close * 2
				ElseIf close >= 3.5 Then 
					close = close * 1.2
				End If
				strength = strength + upOffset(5) * vertical * close
			Case 65
				If close >= 10 Then 
					close = close * 2
				End If
				strength = strength + upOffset(6) * vertical / 1.5 * close
			Case 70
				If close >= 30 Then 
					close = close * 1.3
				End If
				strength = strength + upOffset(7) * vertical * close
		End Select
	End If
	getStrength = strength
	// 44 - 55
End Function


/**
 * --------------------------大漠区-------------------------------
 **/
Sub 大漠注册
	need_ver = "3.1233"
	// 防止被系统精简掉导致的中注册失败
	Set ws=createobject("Wscript.Shell")
	ws.run "regsvr32 atl.dll /s"
	Set ws = nothing
	// 释放插件
	PutAttachment "c:\tmp", "*.*"
	PutAttachment ".\Plugin", "RegDll.dll"
	// 使用RegDll注册
	Call Plugin.RegDll.Reg("c:\tmp\dm.dll")
	set dm = createobject("dm.dmsoft")
	ver = dm.Ver()
	if ver <> need_ver then
		// 先释放先前创建的dm
		set dm = nothing
		// 再尝试用regsvr32 来注册. 这里必须使用绝对路径。以免有别人把dm.dll释放在系统目录.造成版本错误.
		set ws=createobject("Wscript.Shell")
		ws.run "regsvr32 c:\tmp\dm.dll /s"
		set ws=nothing
		Delay 1500  
		// 再判断插件是否注册成功
		set dm = createobject("dm.dmsoft")
		ver = dm.Ver()
		if ver <> need_ver then
			// 这时，已经确认插件注册失败了。 弹出一些调试信息，以供分析.
			messagebox "插件版本错误,当前使用的版本是:"&ver&",插件所在目录是:"&dm.GetBasePath()
			messagebox "请关闭程序,重新打开本程序再尝试"
    		endscript
  		End If
	Else 
  		TracePrint "注册成功"
	End If
	dm.SetPath "c:\tmp"
End Sub

Sub bund(hwnd, mode)
	Set dm = createobject("dm.dmsoft")
	If mode = "1" Then 
		dm_ret = dm.BindWindowEx(hwnd, "dx2", "dx2", "dx", "dx.public.active.message", 4)
	ElseIf mode = "2" Then
		dm_ret = dm.BindWindowEx(hwnd, "dx2", "windows3", "dx", "dx.public.active.message", 4)
	End If
	if dm_ret = 0 then
   		last_error = dm.GetLastError()
		// 如果是WIN7 VISTA WIN2008系统,检测当前系统是否有开启UAC
   		if dm.GetOsType() = 3 then
    		// 有开启UAC的话，尝试关闭
				if dm.CheckUAC() = 1 then
     				if dm.SetUAC(0) = 1 then
          				// 关闭UAC之后，必须重启系统才可以生效
          				messagebox "已经关闭系统UAC设置，必须重启系统才可以生效。点击确定重启系统"
         				// dm.ExitOs 2
                		Delay 2000
         				endscript
     				end if
				end if
   		end if
   		// 具体错误码的含义，可以参考函数GetLastError的说明.
   		MessageBox "绑定失败，错误码是:" & last_error
   		EndScript
		Else 
   		TracePrint "绑定成功"
	End If
End Sub




/**
 * --------------------------备用函数区-------------------------------
 **/
// 获取角色位置，由于三角出现在角色上方3个像素~5个像素的位置，传进来小地图角色位置最上方中间像素（3个中的中间）
Function getPlace(x1, y1, x2, y2, x3, y3, x4, y4)
	getPlace = -1
	If x1 > 0 And y1 > 0 Then 
		If roleInArea(x1 - 2, y1 - 7, x1 + 2, y1 - 2) Then 
			getPlace = 1
			Exit Function
		End If
	End if
	If x2 > 0 And y2 > 0 Then 
		If roleInArea(x2 - 2, y2 - 7, x2 + 2, y2 - 2) Then 
			getPlace = 2
			Exit Function
		End If
	End If
	If x3 > 0 And y3 > 0 Then 
		If roleInArea(x3 - 1, y3 - 6, x3 + 1, y3 - 3) Then 
			getPlace = 3
			Exit Function
		End If
	End If
		
	If x4 > 0 And y4 > 0 Then 
		If roleInArea(x4 - 1, y4 - 6, x4 + 1, y4 - 3) Then 
			getPlace = 4
			Exit Function
		End If
	End If
End Function
Function roleInArea(x1, y1, x2, y2)
	roleInArea = dm.findColor (x1, y1, x2, y2, roleColor + "-101010", 1.0, 0, intX, intY)
End Function