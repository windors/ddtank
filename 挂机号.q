Dimenv tmp
Call ajjl_main
Sub ajjl_main
	Dim hwndFile, slaveNum
	
	// 将master句柄换为数组
	hwndFile = "C:/tmp/hwnds.txt"
	slaveNum = 0
	str = Lib.文件.读取指定行文本内容(hwndFile, 0)
	masterHwnds = Split(str, ",")
	Call 大漠注册


	str = findDdtHwnds()
	TracePrint str
	Dim hwnds, isMaster
	hwnds = Split(str, ",")

	For i = 0 To UBound(hwnds)
		isMaster = false
		tmp = hwnds(i)
		// 去除master句柄
		For j = 0 To UBound(masterHwnds)
			If UBound(masterHwnds) = - 1  Then 
				Exit For 
			End If
			If (tmp = masterHwnds(j)) Then 
				isMaster = true
				Exit For
			End If
		Next
		If isMaster = False Then 
			slaveNum = slaveNum + 1
			
			thread = BeginThread(main)
			TracePrint "等待启动线程中..."
			While tmp <> - 1 
				Delay 100
			Wend
			Delay 100
		End If
	Next
	MsgBox "共找到窗口" & UBound(hwnds) + 1 & "个，启动了挂机号" & slaveNum & "个"
End Sub

Sub main
	Dim hwnd
	hwnd = tmp
	tmp = - 1 
	// 激活窗口并运行代码
	Call bund(hwnd)
	
	dm.moveTo 0, 0
	dm.leftClick
	While True
		// 检测hwnd是否失效
		Call hwndCheck(hwnd)
		// 检测是否需要准备
		Call needPrepare
		// 检测是否需要按p	
		Call needPressPass
		// 检测是否需要将奖励放进背包
		Call needPutInBag
		Delay 1000 * (1 - (Form1.Slider1.Value / 1000))
	Wend
End Sub


// 检测是否需要按p
Sub needPressPass
	dm.FindPic 123, 114, 198, 135, "C:\tmp\该出手了.bmp", 101010, 0.6, 0, intX, intY
	If intX > 0 And intY > 0 Then 
		Dim str
		str = LCase(LTrim(Form1.InputBox1.Text + ""))
		While len(str) > 0
			dm.keyPressChar Left(str, 1)
			str = Right(str, Len(str) - 1)
		Wend
	End If
End Sub

// 检测是否需要准备
Sub needPrepare
	dm.FindPic 886, 430, 970, 548, "C:\tmp\需要准备.bmp", 101010, 0.6, 0, intX, intY
	If intX > 0 And intY > 0 Then 
		// 购买怒气
		dm.moveTo 954, 339
		dm.leftClick 
		dm.leftClick 
		dm.leftClick 
		// 点击准备
		dm.moveTo intX, intY
		dm.leftClick
	End If
End Sub

// 蛋2独有的副本结束后需要将物品放入背包 ok
Sub needPutInBag
	dm.FindPic 385, 218, 407, 246, "C:\tmp\全选进背包.bmp", 101010, 0.8, 0, px, py
	While px > 0 and py > 0
		isEnd = True
		If Form1.CheckBox1.Value = 1 Then 
			// 刷资源模式
			For 10
				For 100
					// 到了结算页面
					dm.FindPic 129, 173, 349, 349, "C:\tmp\时装-久伴.bmp|C:\tmp\时装-偏执.bmp|C:\tmp\时装-七荒.bmp|C:\tmp\时装-小帅.bmp", 000000, 0.7, 0, x, y
					If x > 0 and y > 0 Then 
						dm.moveTo x + 10, y + 5
						dm.leftClick 
						dm.moveTo 0, 0
						TracePrint "找到了一个"
						Delay 1000
					End If
					Delay 1
				Next
				If x < 0 and y < 0 Then 
					TracePrint "已找到全部资源，退出循环"
					Exit For
				End If
			Next
			// 点一下确定按钮
			If Form1.CheckBox2.Value Then 
				Delay 1000
			End If
			
			dm.MoveTo 696, 542
			dm.leftClick 
			Delay 2000
			// 等待结束
		Else
			// 普通模式
			If Form1.CheckBox2.Value Then 
				Delay 2000
			End If
			
			dm.moveTo px, py
			dm.leftClick 
			Delay 1000
		End If
		dm.FindPic 385, 218, 407, 246, "C:\tmp\全选进背包.bmp", 101010, 0.8, 0, px, py
	Wend
End Sub

Sub hwndCheck(hwnd)
    // 窗口已经过期，结束线程即可
	if Plugin.Window.IsWindow(hwnd) = 0 Then
		StopThread GetThreadID()
	End If
End Sub


// 寻找糖果浏览器下的所有ddt句柄，返回字符串，句柄1,句柄2,句柄3
Function findDdtHwnds
	HwndEx = Plugin.Window.SearchEx("Afx:00400000:8:00010003:00000006:00000000", "", 1)
	HwndEx = removeLastChapter(HwndEx)
	TracePrint HwndEx
	parentHwnd = Split(HwndEx, "|")
	
	Dim resultStr
	For i = 0 To UBOUND(parentHwnd)
		str = Plugin.Window.FindEx(parentHwnd(i), 0, "Afx:00400000:8:00010003:00000006:00000000", "")
		If str <> 0 Then 
			str = Plugin.Window.FindEx(str, 0, "Shell Embedding", "")
			If str <> 0 Then 
				str = Plugin.Window.FindEx(str, 0, "Shell DocObject View", "")
				If str <> 0 Then 
					str = Plugin.Window.FindEx(str, 0, "Internet Explorer_Server", "")
					If str <> 0 Then 
						str = Plugin.Window.FindEx(str, 0, "MacromediaFlashPlayerActiveX", "")
						resultStr = resultStr & str & ","
					End If
				End If
			End If
		End If
	Next
	
	findDdtHwnds = removeLastChapter(resultStr)
End Function

Function removeLastChapter(str)
	If Len(str) > 0 Then 
		removeLastChapter = Left(str, Len(str) - 1)
	End If
End Function

/**
 * ---------------------------大漠区-------------------------------
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

Sub bund(hwnd)
	Set dm = createobject("dm.dmsoft")
	TracePrint hwnd
	dm_ret = dm.BindWindowEx(hwnd, "dx2", "dx2", "dx", "dx.public.active.message", 4)
	if dm_ret = 0 then
   	last_error = dm.GetLastError()
	// 如果是WIN7 VISTA WIN2008系统,检测当前系统是否有开启UAC
   	if dm.GetOsType() = 3 then
    	// 有开启UAC的话，尝试关闭
			if dm.CheckUAC() = 1 then
     			if dm.SetUAC(0) = 1 then
          			// 关闭UAC之后，必须重启系统才可以生效
          			messagebox "已经关闭系统UAC设置，必须重启系统才可以生效。点击确定重启系统"
         			dm.ExitOs 2
                	Delay 2000
         			endscript
     			end if
			end if
   	end if
   	// 具体错误码的含义，可以参考函数GetLastError的说明.
   	messagebox "绑定失败，错误码是:"&last_error & ", 窗口句柄为："& hwnd
   	EndScript
	Else 
   	TracePrint "绑定成功"
	end if
End Sub

Sub OnScriptExit()
   dm.UnBindWindow
End Sub
