[General]
SyntaxVersion=2
MacroID=3626b374-83c9-406b-978f-7ef9fe123bc1
[Comment]
������ǰ�������8.0���Ƴ���ȫ�¹���
�����԰��Լ����õĺ������ӳ���д����������úܶ���ű�ȥ����
����������������ö���ű�����һ������޸�һ���͵����޸Ķദ
Ŀǰ����⹦�ܻ��ڲ��Ե��У����κν�������ڰ���������̳�������ַ��http://bbs.ajjl.cn
******ע�⣡���ǹٷ��ṩ������⣬�����޸ģ������Ժ󰴼���������ʱ���������޸ġ�******//
******          ������������⣬������������Ҽ�ѡ���½��������            ******//

[Script]
Function getASCII(s)
	// ��ȡs��ASCII��
	Select Case UCase(s)
		Case " "
			getASCII = 32
		Case "1"
			getASCII = 49
		Case "2"
			getASCII = 50
		Case "3"
			getASCII = 51
		Case "4"
			getASCII = 52
		Case "5"
			getASCII = 53
		Case "6"
			getASCII = 54
		Case "7"
			getASCII = 55
		Case "8"
			getASCII = 56
		Case "9"
			getASCII = 57
		Case "0"
			getASCII = 48
		Case "-"
			getASCII = 189
		Case "="
			getASCII = 187
		Case "A"
			getASCII = 65
		Case "B"
			getASCII = 66
		Case "C"
			getASCII = 67
		Case "D"
			getASCII = 68
		Case "E"
			getASCII = 69
		Case "F"
			getASCII = 70
		Case "G"
			getASCII = 71
		Case "H"
			getASCII = 72
		Case "I"
			getASCII = 73
		Case "J"
			getASCII = 74
		Case "K"
			getASCII = 75
		Case "L"
			getASCII = 76
		Case "M"
			getASCII = 77
		Case "N"
			getASCII = 78
		Case "O"
			getASCII = 79
		Case "P"
			getASCII = 80
		Case "Q"
			getASCII = 81
		Case "R"
			getASCII = 82
		Case "S"
			getASCII = 83
		Case "T"
			getASCII = 84
		Case "U"
			getASCII = 85
		Case "V"
			getASCII = 86
		Case "W"
			getASCII = 87
		Case "X"
			getASCII = 88
		Case "Y"
			getASCII = 89
		Case "Z"
			getASCII = 90
	End Select
End Function


Sub myKeyPressChar(hwnd, keyHwnd, char)
	c = getASCII(char)
	If hwnd = keyHwnd Then 
		dm.keyPress c
	Else 
		
		Plugin.Bkgnd.KeyDown keyHwnd, c
		Plugin.Bkgnd.KeyUp keyHwnd, c
	End If
End Sub

Sub myKeyDownChar(hwnd, keyHwnd, char)
	c = getASCII(char)
	If hwnd = keyHwnd Then 
		dm.keyDown c
	Else 
		
		Plugin.Bkgnd.KeyDown keyHwnd, c
	End If
End Sub

Sub myKeyUpChar(hwnd, keyHwnd, char)
	c = getASCII(char)
	If hwnd = keyHwnd Then 
		dm.keyUp c
	Else 
		
		Plugin.Bkgnd.KeyUp keyHwnd, c
	End If
End Sub

Sub ������ϼ�(������,ģ�ⷽʽ)
    //���ӣ�Call lib.����.������ϼ�("Ctrl + Alt + A",0)
    //ģ�ⷽʽ����0��ͨģ�⣬1Ӳ��ģ�⣬2����ģ�⡿
    //���ࡾ�����롿�����������
    Dim ���Ƽ�,������,���ܼ�,�����,��ĸ��,���ּ�,���ż�
    ���Ƽ� = "CTRL 17,ALT 18,SHIFT 16,LCTRL 162,LALT 164,LSHIFT 160,RCTRL 163,RALT 165,RSHIFT 161,WIN 91"
    ������ = "CTRL 17,ALT 18,SHIFT 16,LCTRL 162,LALT 164,LSHIFT 160,RCTRL 163,RALT 165,RSHIFT 161"
    ����� = "DOWN 40,UP 38,LEFT 37,RIGHT 39"
    ���ܼ� = "F1 112,F2 113,F3 114,F4 115,F5 116,F6 117,F7 118,F8 119,F9 120,F10 121,F11 122,F12 123,HOME 36,END 35,PAGEDOWN 34,PAGEUP 33,ESC 27,ENTER 13,SPACE 32"
    ��ĸ�� = "A 65,B 66,C 67,D 68,E 69,F 70,G 71,H 72,I 73,J 74,K 75,L 76,M 77,N 78,O 79,P 80,Q 81,R 82,S 83,T 84,U 85,V 86,W 87,X 88,Y 89,Z 90"
    ���ּ� = "0 48,1 49,2 50,3 51,4 52,5 53,6 54,7 55,8 56,9 57"
    ���ż� = "~ 192,` 192,- 189,= 187,[ 219,] 221,\ 220,/ 191,? 191,< 188,> 190"
    //��ȫ���1
    Dim ת�ɴ�д,ȥ���ո�,i
    ת�ɴ�д = UCase(������)
    ȥ���ո� = Replace(ת�ɴ�д," ","")
    Dim �ָ�Ӻ�,�Ӻ�����
    �ָ�Ӻ� = Split(ȥ���ո�,"+") 
    �Ӻ����� = UBound(�ָ�Ӻ�)
    If �Ӻ�����>0 And �Ӻ�����<3 Then        
        If InStr(���Ƽ�,�ָ�Ӻ�(0))>0 And �ָ�Ӻ�(0)<>"" Then 
            //������Ƽ���
            Dim ��,����
            �� = Split(���Ƽ�,",") 
            For i=0 To UBound(��)
                If InStr(��(i),�ָ�Ӻ�(0))>0 Then
                    ���� = Split(��(i)," ")   
                    Exit For 
                End If 
            Next       
            Dim ��,����,����
            If �Ӻ����� = 1 Then   
                If �ָ�Ӻ�(1)<>�ָ�Ӻ�(0) And �ָ�Ӻ�(1)<>"" Then    
                    Dim �Ϸ�1(4)
                    �Ϸ�1(0) = InStr(���ܼ�,�ָ�Ӻ�(1))
                    �Ϸ�1(1) = InStr(�����,�ָ�Ӻ�(1))
                    �Ϸ�1(2) = InStr(��ĸ��,�ָ�Ӻ�(1))
                    �Ϸ�1(3) = InStr(���ּ�,�ָ�Ӻ�(1))
                    �Ϸ�1(4) = InStr(���ż�,�ָ�Ӻ�(1))
                    //��ȫ���2
                    If �Ϸ�1(0)>0 Or �Ϸ�1(1)>0 Or �Ϸ�1(2)>0 Or �Ϸ�1(3)>0 Or �Ϸ�1(4)>0 Then  
                        //���㰴������
                        �� = Split(��ĸ��,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(1) & " ")>0 Then
                                ���� = Split(��(i)," ")  
                                Goto ���1 
                            End If 
                        Next  
                        �� = Split(���ּ�,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(1) & " ")>0 Then
                                ���� = Split(��(i)," ") 
                                Goto ���1 
                            End If 
                        Next         
                        �� = Split(�����,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(1) & " ")>0 Then
                                ���� = Split(��(i)," ")   
                                Goto ���1 
                            End If 
                        Next  
                        �� = Split(���ܼ�,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(1) & " ")>0 Then
                                ���� = Split(��(i)," ")
                                Goto ���1 
                            End If 
                        Next 
                        �� = Split(���ż�,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(1) & " ")>0 Then
                                ���� = Split(��(i)," ")  
                                Goto ���1 
                            End If 
                        Next             
                        Rem ���1 
                        //��������ϼ�
                        If ģ�ⷽʽ = 0 Then
                            KeyDown Clng(����(1)), 1
                            KeyPress Clng(����(1)), 1
                            KeyUp Clng(����(1)), 1
                        ElseIf ģ�ⷽʽ = 1 Then
                            KeyDownH Clng(����(1)), 1
                            KeyPressH Clng(����(1)), 1
                            KeyUpH Clng(����(1)), 1
                        ElseIf ģ�ⷽʽ = 2 Then 
                            KeyDownS Clng(����(1)), 1
                            KeyPressS Clng(����(1)), 1
                            KeyUpS Clng(����(1)), 1
                        End If 
                        Exit Sub
                    End If 
                End If 
            ElseIf �Ӻ����� = 2 Then
                If �ָ�Ӻ�(2)<>�ָ�Ӻ�(1) And �ָ�Ӻ�(2)<>"" Then 
                    Dim �Ϸ�2(5)
                    �Ϸ�2(0) = InStr(������,�ָ�Ӻ�(2))
                    �Ϸ�2(1) = InStr(���ܼ�,�ָ�Ӻ�(2))
                    �Ϸ�2(2) = InStr(�����,�ָ�Ӻ�(2))
                    �Ϸ�2(3) = InStr(��ĸ��,�ָ�Ӻ�(2))
                    �Ϸ�2(4) = InStr(���ּ�,�ָ�Ӻ�(2))
                    �Ϸ�2(5) = InStr(���ż�,�ָ�Ӻ�(2))
                    //��ȫ���3
                    If �Ϸ�2(0)>0 Or �Ϸ�2(1)>0 Or �Ϸ�2(2)>0 Or �Ϸ�2(3)>0 Or �Ϸ�2(4)>0 Or �Ϸ�2(5)>0 Then
                        //���㰴������
                        �� = Split(��ĸ��,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(2) & " ")>0 Then
                                ���� = Split(��(i)," ")  
                                Goto ���2 
                            End If 
                        Next  
                        �� = Split(���ּ�,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(2) & " ")>0 Then
                                ���� = Split(��(i)," ") 
                                Goto ���2 
                            End If 
                        Next 
                        �� = Split(�����,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(2) & " ")>0 Then
                                ���� = Split(��(i)," ")   
                                Goto ���2 
                            End If 
                        Next    
                        �� = Split(���ܼ�,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(2) & " ")>0 Then
                                ���� = Split(��(i)," ")
                                Goto ���2 
                            End If 
                        Next  
                        �� = Split(���ż�,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(2) & " ")>0 Then
                                ���� = Split(��(i)," ")  
                                Goto ���2 
                            End If 
                        Next 
                        Rem ���2
                        //���㸨������
                        �� = Split(������,",") 
                        For i=0 To UBound(��)
                            If InStr(��(i),�ָ�Ӻ�(1) & " ")>0 Then
                                ���� = Split(��(i)," ")
                                Exit For 
                            End If 
                        Next  
                        //��������ϼ�
                        If ģ�ⷽʽ = 0 Then
                            KeyDown Clng(����(1)), 1
                            KeyDown Clng(����(1)), 1
                            KeyPress Clng(����(1)), 1
                            KeyUp Clng(����(1)), 1
                            KeyUp Clng(����(1)), 1
                        ElseIf ģ�ⷽʽ = 1 Then
                            KeyDownH Clng(����(1)), 1
                            KeyDownH Clng(����(1)), 1
                            KeyPressH Clng(����(1)), 1
                            KeyUpH Clng(����(1)), 1
                            KeyUpH Clng(����(1)), 1
                        ElseIf ģ�ⷽʽ = 2 Then
                            KeyDownS Clng(����(1)), 1
                            KeyDownS Clng(����(1)), 1
                            KeyPressS Clng(����(1)), 1
                            KeyUpS Clng(����(1)), 1
                            KeyUpS Clng(����(1)), 1
                        End If 
                        Exit Sub
                    End If 
                End If 
            End If
            //ͨ������            
        End If
    End If 
End Sub


Sub ���̰�����(������,ģ�ⷽʽ,������ʱ)
    //���ӣ�Call lib.����.���̰�����("A,B,C,SPACE,D,E,F,G",0,50)
    //ģ�ⷽʽ����0��ͨģ�⣬1Ӳ��ģ�⣬2����ģ�⡿
    //���ࡾ�����롿�����������
    Dim ���Ƽ�,������,���ܼ�,�����,��ĸ��,���ּ�,���ż�,��ϼ�
    ���Ƽ� = "CTRL 17,ALT 18,SHIFT 16,LCTRL 162,LALT 164,LSHIFT 160,RCTRL 163,RALT 165,RSHIFT 161,WIN 91"
    ���ܼ� = "F1 112,F2 113,F3 114,F4 115,F5 116,F6 117,F7 118,F8 119,F9 120,F10 121,F11 122,F12 123,HOME 36,END 35,PAGEDOWN 34,PAGEUP 33,ESC 27,ENTER 13,SPACE 32"
    ����� = "DOWN 40,UP 38,LEFT 37,RIGHT 39"
    ��ĸ�� = "A 65,B 66,C 67,D 68,E 69,F 70,G 71,H 72,I 73,J 74,K 75,L 76,M 77,N 78,O 79,P 80,Q 81,R 82,S 83,T 84,U 85,V 86,W 87,X 88,Y 89,Z 90"
    ���ּ� = "0 48,1 49,2 50,3 51,4 52,5 53,6 54,7 55,8 56,9 57"
    ���ż� = "~ 192,` 192,- 189,= 187,[ 219,] 221,\ 220,/ 191,? 191,< 188,> 190"
    ��ϼ� = ���Ƽ� &","& ���ܼ� &","& ����� &","& ��ĸ�� &","& ���ּ� &","& ���ż�
    //��ȫ���
    Dim ת�ɴ�д,ȥ���ո�
    ת�ɴ�д = UCase(������)
    ȥ���ո� = Replace(ת�ɴ�д," ","")
    //����
    Dim �ָ��,��������,�ָ����,����,��������
    �ָ�� = Split(ȥ���ո�,",")
    �������� = UBound(�ָ��) 
    //����
    �ָ���� = Split(��ϼ�,",")
    �������� = UBound(�ָ����)
    Dim i,k,n
    For i=0 To �������� 
        //�������
        For k=0 To ��������
            ���� = Split(�ָ����(k)," ") 
            If ����(0) = �ָ��(i) Then 
                If ģ�ⷽʽ = 0 Then 
                    KeyPress Clng(����(1)), 1
                ElseIf ģ�ⷽʽ = 1 Then
                    KeyPressH Clng(����(1)), 1 
                ElseIf ģ�ⷽʽ = 2 Then
                    KeyPressS Clng(����(1)), 1 
                End If 
                n = Plugin.Sys.GetTime() + ������ʱ
                Do   
                    Delay 5
                loop Until Plugin.Sys.GetTime() >= n
                Exit For
            End If 
        Next
    Next 
End Sub 




Sub KeyList(������,ģ�ⷽʽ,������ʱ)
    //���ӣ�Call lib.����.KeyList("aA@2?"">.',/|\=-+_)(*&^QAsD",0,50)
    //��Ҫע����ǣ�������һ������ʱ��"����������һ�ԣ�""��
    //ģ�ⷽʽ����0��ͨģ�⣬1Ӳ��ģ�⣬2����ģ�⡿
    Dim ����(46)
    ����(0) ="a��A��65"
    ����(1) ="b��B��66"
    ����(2) ="c��C��67"
    ����(3) ="d��D��68"
    ����(4) ="e��E��69"
    ����(5) ="f��F��70"
    ����(6) ="g��G��71"
    ����(7) ="h��H��72"
    ����(8) ="i��I��73"
    ����(9) ="j��J��74"
    ����(10)="k��K��75"
    ����(11)="l��L��76"
    ����(12)="m��M��77"
    ����(13)="n��N��78"
    ����(14)="o��O��79"
    ����(15)="p��P��80"
    ����(16)="q��Q��81"
    ����(17)="r��R��82"
    ����(18)="s��S��83"
    ����(19)="t��T��84"
    ����(20)="u��U��85"
    ����(21)="v��V��86"
    ����(22)="w��W��87"
    ����(23)="x��X��88"
    ����(24)="y��Y��89"
    ����(25)="z��Z��90"
    ����(26)="`��~��192"
    ����(27)="1��!��49"
    ����(28)="2��@��50"
    ����(29)="3��#��51"
    ����(30)="4��$��52"
    ����(31)="5��%��53"
    ����(32)="6��^��54"
    ����(33)="7��&��55"
    ����(34)="8��*��56"
    ����(35)="9��(��57"
    ����(36)="0��)��48"
    ����(37)="-��_��189"
    ����(38)="=��+��187"
    ����(39)="[��{��219"
    ����(40)="]��}��221"
    ����(41)="\��|��220"
    ����(42)=";��:��186"
    ����(43)="'��""��222"
    ����(44)=",��<��188"
    ����(45)=".��>��190"
    ����(46)="/��?��191"
    //Dim KeyS()
    Dim ����,�ж�,i,m,n
    ����=Len(������)
    ReDim KeyS(����)
    For i=0 to ����-1
        KeyS(i)=Mid(������,i+1,1)
        �ж�=False
        For n=0 to 46
            MyKeyS=Split(����(n),"��")
            If KeyS(i)=MyKeyS(0) Then
                �ж�=True
                If ģ�ⷽʽ = 0 Then 
                    KeyPress Clng(MyKeyS(2)), 1
                ElseIf ģ�ⷽʽ = 1 Then
                    KeyPressH Clng(MyKeyS(2)), 1
                ElseIf ģ�ⷽʽ = 2 Then
                    KeyPressS Clng(MyKeyS(2)), 1
                End If
                Exit For
            ElseIf KeyS(i)=MyKeyS(1) Then ://��Ҫ��סShift����ģ��
                �ж�=True
                If ģ�ⷽʽ = 0 Then 
                    KeyDown 16, 1
                    KeyPress Clng(MyKeyS(2)), 1
                    KeyUp 16, 1
                ElseIf ģ�ⷽʽ = 1 Then
                    KeyDownH 16, 1
                    KeyPressH Clng(MyKeyS(2)), 1
                    KeyUpH 16, 1
                ElseIf ģ�ⷽʽ = 2 Then
                    KeyDownS 16, 1
                    KeyPressS Clng(MyKeyS(2)), 1
                    KeyUpS 16, 1
                End If
                Exit For
            End If
        Next
        m = Plugin.Sys.GetTime() + ������ʱ
        Do   
            Delay 5
        loop Until Plugin.Sys.GetTime() >= m
        If �ж�=False Then Exit Sub
    Next
End Sub

//������һֻ��
//���ڣ�2009.12.24
//�޸ģ�2011.04.06


