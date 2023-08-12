Dim hwnd
Dim round
Dim position
Dim toward
Dim n
Dim checkTimes
Dim antFirst
Call init
Sub init
	checkTimes = 0
	n = 0
	If (Form1.InputBox1.Text + 0) <> - 1  Then 
		MessageBox "选择了手动指定句柄"
		hwnd = Form1.InputBox1.Text
	Else 
		hwnd = Plugin.Window.MousePoint
	End If
	If (Plugin.Window.GetClass(hwnd) <> "MacromediaFlashPlayerActiveX") Then 
		MessageBox "未检测到窗口，请重新运行脚本，启动时鼠标需要放在游戏内！"
	Else 
		Call main
	End If
End Sub

Sub main
	Call 大漠注册
	Call bund(hwnd)
	// 将主句柄保存
	Call saveMaster(hwnd)
	dm.moveTo 0, 0
	dm.leftClick 
	While True
		checkTimes = checkTimes + 1
		If checkTimes > 100 Then 
			checkTimes = 0
		End If
		// 检测hwnd是否失效
		Call hwndCheck(hwnd)
		
		
		// 检测是否需要选地图
		Call needPrepare
		Call needChoseMap
		Call needCloseTip
		Call needCloseEmail
		// 检测是否需要出手
		Call needAttack
		// 检测是否需要将奖励放进背包
		If checkTimes Mod 5 = 0 Then 
			Call needPutInBag
		End If
		Delay 700 * (1 - (Form1.Slider1.Value / 1000))
	Wend
End Sub

Sub 要打的副本
	round = 0
	position = 0
	toward = 0
	Form1.Label4.Caption = "第" & (n + 1) & "次副本，开始！"
	If Form1.ComboBox1.ListIndex > 11 Then 
		// 后面的本要先点击这个
		dm.moveTo 806, 390
		dm.leftClick 
	End If
	Select Case Form1.ComboBox1.ListIndex
		Case 0
			// 新手本
			dm.moveTo 319, 321
		Case 1
			// 蚂蚁本
			dm.moveTo 460, 322
		Case 2
			// 小鸡本
			dm.moveTo 580, 319
		Case 3
			// 波谷本
			dm.moveTo 721,320 
		Case 4
			// 邪神
			dm.moveTo 318, 374
		Case 5
			// 堡垒
			dm.moveTo 458,376
		Case 6
			// 龙巢
			dm.moveTo 592, 377
		Case 7
			// 运动会
			dm.moveTo 724, 373
		Case 8
			// 时空
			dm.moveTo 325, 432
		Case 9
			// 勇士竞技场
			dm.moveTo 459, 425
		Case 10
			// 世界杯
			dm.moveTo 587, 426
		Case 11
			// 魔法堡垒
			dm.moveTo 720, 425
		Case 12
			// 冰雪之城
			dm.moveTo 323, 325
		Case 13
			// 魔法森林
			dm.moveTo 459, 325
		Case 14
			// 沙丘幻境
			dm.moveTo 589, 329
		Case 15
			// 血色
			dm.moveTo 728, 334
		Case 16
			// 冰雪城堡
			dm.moveTo 322, 380
		Case 17
			// 四大神兽
			dm.moveTo 455, 384
		Case 18
			// 天空之城
			dm.moveTo 590, 379
		Case 19
			// 大闹天宫
			dm.moveTo 726, 379
		Case 20
			// 龙舟
			dm.moveTo 327, 435
		Case 21
			// 人鱼
			dm.moveTo 453, 431
	End Select
	dm.leftClick 
	// 难度
	dm.moveTo 366, 504
	dm.leftClick
	dm.moveTo 735, 568
	If Form1.CheckBox3.Value Then 
		Delay 2000
	End If
	dm.leftClick 
	Delay 300
End Sub



Sub 打副本策略
	round = round + 1
	Form1.Label4.Caption = "第" & (n + 1) & "次副本，第" & round & "回合"
	Select Case Form1.ComboBox1.ListIndex
		Case 0
			If round = 1 Then 
				dm.keyDownChar "d"
				Delay 800
				dm.keyUpChar "d"
			End If
			Call 打手攻击(1500)
		Case 1
			If round = 1 Then 
				For 7
					dm.keyPressChar "w"
				Next
			End If
			Call 打手攻击(2000)
		Case 2
			If round = 1 Then 
				dm.keyDownChar "d"
				Delay 2000
				dm.keyUpChar "d"
			Else 
				dm.keyDownChar "d"
				Delay 100
				dm.keyUpChar "d"
			End If
			Call attack(60)
		Case 3
			If round = 1 Then 
				dm.keyDownChar "d"
				Delay 800
				dm.keyUpChar "d"
				For 8
					dm.keyPressChar "w"
				Next
			End If
			Call 打手攻击(1500)
		Case 4
			// 强化本
			If round = 1 Then 
				position = getPlace(820, 89, 850, 89, 954, 89, 982, 89)
				While position < 1
					position = getPlace(820, 89, 850, 89, 954, 89, 982, 89)
				Wend
				If position = 1 Then 
					For 7
						dm.keyPress 87
					Next	
				ElseIf position = 2 Or position = 3 Then 
					For 10
						dm.keyPress 87
					Next
				End If
			End If
			Call 打手攻击(1700)
		Case 5
			If round = 1 Then 
				position = getPlace(873, 96, 891, 96, 905, 96, 923, 96)
				While position < 0
					position = getPlace(873, 96, 891, 96, 905, 96, 923, 96)
				Wend
				If position = 1 Or position = 2 Then 
					// 40 55
					// 40 50
					For 15
						dm.keyPressChar "w"
					Next
				ElseIf position = 3 Then
					// 50, 45~50
					For 23
						dm.keyPressChar "w"
					Next
				ElseIf position = 4 Then 
					// 55, 43
					For 26
						dm.keyPressChar "w"
					Next
				End If
			End If
			If position = 1 Then 
				Call attack(55)
			ElseIf position = 2 Then
				Call attack(50)
			ElseIf position = 3 Then
				Call attack(45)
			ElseIf position = 4 Then
				Call attack(43)
			End If
		Case 6
			// 走左最左面，然后50力度即可
			If round = 1 Then 
				dm.keyDownChar "a"
				Delay 9000
				dm.keyUpChar "a"
				dm.keyPressChar "p"
				dm.keyPressChar "d"
			Else 
				Call attack(50)
			End If
		Case 7
			// 走到最右面，65 56; 20 54
			If round = 1 Then 
				dm.keyDownChar "d"
				Delay 9000
				dm.keyUpChar "d"
				dm.keyPressChar "p"
				dm.keyPressChar "a"
				For 3
					dm.keyPressChar "w"
				Next
			Else 
				Call attack(54)
			End If
		Case 8
			If dm.getColor(984, 90) = "ff0000" Then 
				// 右面的没有打死
				If toward = 0 Or toward = 1 Then 
					dm.keyPressChar "d"
					toward = 2
				End If
				Call attack(70)
			ElseIf dm.getColor(878, 90) = "ff0000" Then
				// 左面的没有被打死
				If toward = 0 Or toward = 2 Then 
					dm.keyPressChar "a"
					toward = 1
				End If
				Call attack(70)
			End If
			// 再检测左面的有没有打死
	End Select
End Sub

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
	roleInArea = dm.findColor (x1, y1, x2, y2, "00CCFF-101010", 1.0, 0, intX, intY)
End Function

Sub 打手穿甲攻击(spaceDelay)
	If Form1.CheckBox2.Value = 1 Then 
		Call 打手攻击(spaceDelay)
	Else 
		dm.keyPress 50
		For 10
			dm.keyPress 51	
		Next
		For 10
			dm.keyPress 56
		Next
		For 10
			dm.keyPress 53
		Next
		For 10
			dm.keypress 52
		Next
		
		dm.keyDown 32
		Delay spaceDelay
		dm.keyUp 32
	End If
End Sub

Sub 打手攻击(spaceDelay)
	Dim str
	str = LCase(LTrim(Form1.InputBox2.Text + ""))
	While len(str) > 0
		dm.keyPressChar Left(str, 1)
		str = Right(str, Len(str) - 1)
	Wend
	dm.keyDown 32
	Delay spaceDelay
	dm.keyUp 32
End Sub

Sub attack(strength)
	Dim str
	str = LCase(LTrim(Form1.InputBox2.Text + ""))
	While len(str) > 0
		dm.keyPressChar Left(str, 1)
		str = Right(str, Len(str) - 1)
	Wend
	// 计算力度
	If strength > 95 Then 
		strength = 95
	End If
	dm.keyDown 32
	While dm.getColor(159 + 4.97 * strength, 596) <> "6d2802"
		Delay 1
	Wend
	dm.keyUp 32
	
End Sub


Sub needChoseMap
	// 先点击，然后再检测
	dm.FindPic 511, 437, 685, 527, "C:\tmp\随机地图.bmp", 101010, 0.8, 0, intX, intY
	If intX > 0 And intY > 0 Then 
		dm.moveTo intX + 10, intY + 10
		dm.leftClick 
		Delay 300
		dm.FindPic 261, 71, 349, 122, "C:\tmp\选副本页面.bmp", 101010, 0.8, 0, intX, intY
		If intX > 0 And intY > 0 Then 
			TracePrint "第" & (n + 1) & "次" & "副本，开始"
			Call 要打的副本
		End If
	End If
	dm.FindPic 261, 71, 349, 122, "C:\tmp\选副本页面.bmp", 101010, 0.8, 0, intX, intY
		If intX > 0 And intY > 0 Then 
			TracePrint "第" & (n + 1) & "次" & "副本，开始"
			Call 要打的副本
		End If
	dm.FindPic 891, 454, 981, 495, "C:\tmp\房主开始.bmp", 101010, 0.6, 0, intX, intY
	If intX > 0 And intY > 0 Then 
		dm.moveTo intX + 10, intY + 10
		dm.leftClick 
	End If

	dm.FindPic 893, 454, 980, 508, "C:\tmp\房主开始2.bmp", 101010, 0.6, 0, intX, intY
	If intX > 0 And intY > 0 Then 
		dm.moveTo intX, intY
		dm.leftClick 
	End If
End Sub

Sub needCloseTip
	dm.FindPic 650, 228, 691, 258, "C:\tmp\单人模式提示.bmp", 101010, 0.8, 0, intX, intY
	If intX > 0 And intY > 0 Then 
		dm.moveTo 432, 345
		dm.leftClick
	End If
End Sub

Sub needCloseEmail
	dm.FindPic 125, 86, 344, 113, "C:\tmp\邮件.bmp", 101010, 0.8, 0, intX, intY
	If intX > 0 And intY > 0 Then 
		dm.moveTo 836, 52
		dm.leftClick
	End If
End Sub

// 检测是否需要准备
Sub needPrepare
	dm.FindPic 886, 430, 970, 548, "C:\tmp\需要准备.bmp", 101010, 0.6, 0, intX, intY
	If intX > 0 And intY > 0 Then 
		// 购买怒气
		//dm.moveTo 954, 339
		//dm.leftClick 
		//dm.leftClick 
		//dm.leftClick 
		// 点击准备
		dm.moveTo intX, intY
		dm.leftClick
	End If
End Sub

Sub needAttack
	dm.FindPic 123, 114, 198, 135, "C:\tmp\该出手了.bmp", 101010, 0.8, 0, intX, intY
	If intX > 0 And intY > 0 Then 
		Call 打副本策略
	End If
End Sub

// 蛋2独有的副本结束后需要将物品放入背包 ok
Sub needPutInBag
	Dim isEnd
	isEnd = False
	dm.FindPic 385, 218, 407, 246, "C:\tmp\全选进背包.bmp", 101010, 0.8, 0, px, py
	While px > 0 and py > 0
		TracePrint "检测到结束窗口"
		isEnd = True
		If n = 0 and antFirst Then 
			// 点一下确定按钮
			dm.MoveTo 696, 542
			dm.leftClick 
			Delay 2000
		Else 
			If Form1.CheckBox1.Value = 1 Then 
			// 刷资源模式
				For 10
					For 50
						// 到了结算页面
						dm.FindPic 129, 173, 349, 349, "C:\tmp\神器天书.bmp|C:\tmp\武器碎片.bmp|C:\tmp\银币.bmp|C:\tmp\半神精华.bmp|C:\tmp\金币碎片.bmp|C:\tmp\翻拍卡碎片.bmp|C:\tmp\熔炼公式1.bmp", 101010, 0.5, 0, x, y
						If x > 0 and y > 0 Then 
							dm.moveTo x + 10, y + 5
							dm.leftClick 
							TracePrint "找到了一个"
							Delay 3000
						End If
					Next
					If x < 0 and y < 0 Then 
						TracePrint "已找到全部资源，退出循环"
						Exit For
					End If
				Next
				// 点一下确定按钮
				dm.MoveTo 696, 542
				dm.leftClick 
				Delay 2000
				// 等待结束
			Else
				// 普通模式
				dm.moveTo px, py
				dm.leftClick 
				Delay 5000
			End If
		End If
		dm.FindPic 385, 218, 407, 246, "C:\tmp\全选进背包.bmp", 101010, 0.8, 0, px, py
	Wend
	If isEnd Then 
		n = n + 1
		Form1.Label4.Caption = "第" & n & "次副本，结束！"
	End If
End Sub

Sub hwndCheck(hwnd)
    // 窗口已经过期，结束线程即可
	If Plugin.Window.IsWindow(hwnd) = 0  Then 
		MsgBox "检测到窗口关闭，即将结束本次运行"
		ExitScript
	End If
End Sub


// 保存所有打手的句柄，在运行挂机脚本时排除这些句柄
Sub saveMaster(hwnd)
	TracePrint hwnd
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
			TracePrint resultStr
			Call Lib.文件.替换指定行文本内容(hwndFile,resultStr,0)
		End If
	End If
End Sub

Function strToArray(str)
	strToArray = Split(str, ",")
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
	dm_ret = dm.BindWindowEx(hwnd, "dx2", "dx2", "dx", "dx.public.active.message", Form1.InputBox3.Text + 0)
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
	end if
End Sub

Sub OnScriptExit()
   dm.UnBindWindow
End Sub