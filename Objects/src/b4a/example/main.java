package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;
    public static boolean dontPause;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(this, processBA, wl, true))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create " + (isFirst ? "(first time)" : "") + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        if (!dontPause)
            BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        else
            BA.LogInfo("** Activity (main) Pause event (activity is not paused). **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        if (!dontPause) {
            processBA.setActivityPaused(true);
            mostCurrent = null;
        }

        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
            main mc = mostCurrent;
			if (mc == null || mc != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
            if (mc != mostCurrent)
                return;
		    processBA.raiseEvent(mc._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.objects.B4XViewWrapper.XUI _xui = null;
public static anywheresoftware.b4a.objects.Timer _tmrtoque = null;
public static anywheresoftware.b4a.objects.Timer _tmrjogo = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bt_action = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bt_ini = null;
public anywheresoftware.b4a.objects.ImageViewWrapper[] _fundos = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _img_bomb = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _img_explod = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _img_flag = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _img_nobomb = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _img_vazio = null;
public anywheresoftware.b4a.objects.ButtonWrapper[] _btns = null;
public anywheresoftware.b4a.objects.LabelWrapper[] _lbls = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_bandeira = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_time = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_info = null;
public static int[] _tipos = null;
public static int _bombardasmarcadas = 0;
public static int _celula = 0;
public static int _idxpendente = 0;
public static int _segundos = 0;
public static int _totalbandeiras = 0;
public static boolean _jogoiniciado = false;
public static boolean _jogopausado = false;
public static boolean _visivel = false;
public anywheresoftware.b4a.objects.collections.List _viewsdinamicas = null;
public static int _pad = 0;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 32;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 33;BA.debugLine="Activity.LoadLayout(\"campo_minado\")";
mostCurrent._activity.LoadLayout("campo_minado",mostCurrent.activityBA);
 //BA.debugLineNum = 36;BA.debugLine="img_bomb.Visible = False";
mostCurrent._img_bomb.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 37;BA.debugLine="img_vazio.Visible = False";
mostCurrent._img_vazio.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 38;BA.debugLine="img_nobomb.Visible = False";
mostCurrent._img_nobomb.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 39;BA.debugLine="img_explod.Visible = False";
mostCurrent._img_explod.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 40;BA.debugLine="img_flag.Visible = False";
mostCurrent._img_flag.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 41;BA.debugLine="visivel = False";
_visivel = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 44;BA.debugLine="tmrToque.Initialize(\"tmrToque\", 350)";
_tmrtoque.Initialize(processBA,"tmrToque",(long) (350));
 //BA.debugLineNum = 45;BA.debugLine="tmrToque.Enabled = False";
_tmrtoque.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 46;BA.debugLine="tmrJogo.Initialize(\"tmrJogo\", 1000)";
_tmrjogo.Initialize(processBA,"tmrJogo",(long) (1000));
 //BA.debugLineNum = 47;BA.debugLine="tmrJogo.Enabled = False";
_tmrjogo.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 50;BA.debugLine="celula = Activity.Width / 9";
_celula = (int) (mostCurrent._activity.getWidth()/(double)9);
 //BA.debugLineNum = 51;BA.debugLine="pad = celula * 0.1";
_pad = (int) (_celula*0.1);
 //BA.debugLineNum = 53;BA.debugLine="viewsDinamicas.Initialize";
mostCurrent._viewsdinamicas.Initialize();
 //BA.debugLineNum = 55;BA.debugLine="ResetarEstado ' Centraliza a limpeza de variáveis";
_resetarestado();
 //BA.debugLineNum = 56;BA.debugLine="CriarGrade 'Fundo (ImageView)| Label (Número de v";
_criargrade();
 //BA.debugLineNum = 59;BA.debugLine="bt_ini.Text = \"Iniciar\"";
mostCurrent._bt_ini.setText(BA.ObjectToCharSequence("Iniciar"));
 //BA.debugLineNum = 60;BA.debugLine="lbl_time.Left = Activity.Width - lbl_time.Width";
mostCurrent._lbl_time.setLeft((int) (mostCurrent._activity.getWidth()-mostCurrent._lbl_time.getWidth()));
 //BA.debugLineNum = 61;BA.debugLine="lbl_time.Top = 5dip";
mostCurrent._lbl_time.setTop(anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (5)));
 //BA.debugLineNum = 62;BA.debugLine="lbl_bandeira.Left = 5dip";
mostCurrent._lbl_bandeira.setLeft(anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (5)));
 //BA.debugLineNum = 63;BA.debugLine="lbl_bandeira.Top = 5dip";
mostCurrent._lbl_bandeira.setTop(anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (5)));
 //BA.debugLineNum = 64;BA.debugLine="bt_ini.Left = (Activity.Width / 2) - (bt_ini.Widt";
mostCurrent._bt_ini.setLeft((int) ((mostCurrent._activity.getWidth()/(double)2)-(mostCurrent._bt_ini.getWidth()/(double)2)));
 //BA.debugLineNum = 65;BA.debugLine="bt_ini.Top = btns(80).top + btns(80).Height  + 80";
mostCurrent._bt_ini.setTop((int) (mostCurrent._btns[(int) (80)].getTop()+mostCurrent._btns[(int) (80)].getHeight()+80));
 //BA.debugLineNum = 66;BA.debugLine="bt_action.top = bt_ini.Top  + 50";
mostCurrent._bt_action.setTop((int) (mostCurrent._bt_ini.getTop()+50));
 //BA.debugLineNum = 67;BA.debugLine="bt_action.Left = (bt_ini.Left + bt_ini.Width) / 2";
mostCurrent._bt_action.setLeft((int) ((mostCurrent._bt_ini.getLeft()+mostCurrent._bt_ini.getWidth())/(double)2));
 //BA.debugLineNum = 70;BA.debugLine="lbl_info.Left = btns(0).Left";
mostCurrent._lbl_info.setLeft(mostCurrent._btns[(int) (0)].getLeft());
 //BA.debugLineNum = 71;BA.debugLine="lbl_info.Top = btns (0).top";
mostCurrent._lbl_info.setTop(mostCurrent._btns[(int) (0)].getTop());
 //BA.debugLineNum = 72;BA.debugLine="lbl_info.Width  = btns(8).Left + btns(8).Width -";
mostCurrent._lbl_info.setWidth((int) (mostCurrent._btns[(int) (8)].getLeft()+mostCurrent._btns[(int) (8)].getWidth()-mostCurrent._btns[(int) (0)].getLeft()));
 //BA.debugLineNum = 73;BA.debugLine="lbl_info.Height = btns(80).Top + btns(80).Height";
mostCurrent._lbl_info.setHeight((int) (mostCurrent._btns[(int) (80)].getTop()+mostCurrent._btns[(int) (80)].getHeight()-mostCurrent._btns[(int) (0)].getTop()+100));
 //BA.debugLineNum = 74;BA.debugLine="End Sub";
return "";
}
public static String  _alternarpausa(boolean _pausar) throws Exception{
 //BA.debugLineNum = 88;BA.debugLine="Sub AlternarPausa(Pausar As Boolean)";
 //BA.debugLineNum = 89;BA.debugLine="jogoPausado = Pausar";
_jogopausado = _pausar;
 //BA.debugLineNum = 90;BA.debugLine="tmrJogo.Enabled = Not(Pausar)";
_tmrjogo.setEnabled(anywheresoftware.b4a.keywords.Common.Not(_pausar));
 //BA.debugLineNum = 91;BA.debugLine="tmrToque.Enabled = False";
_tmrtoque.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 92;BA.debugLine="If Pausar Then bt_ini.Text = \"Continuar\" Else bt_";
if (_pausar) { 
mostCurrent._bt_ini.setText(BA.ObjectToCharSequence("Continuar"));}
else {
mostCurrent._bt_ini.setText(BA.ObjectToCharSequence("Pausar"));};
 //BA.debugLineNum = 93;BA.debugLine="End Sub";
return "";
}
public static String  _atualizarvisualcelula(int _idx,int _count) throws Exception{
 //BA.debugLineNum = 187;BA.debugLine="Sub AtualizarVisualCelula(idx As Int, count As Int";
 //BA.debugLineNum = 188;BA.debugLine="If tipos(idx) = 1 Then";
if (_tipos[_idx]==1) { 
 //BA.debugLineNum = 189;BA.debugLine="fundos(idx).Bitmap = img_bomb.Bitmap";
mostCurrent._fundos[_idx].setBitmap(mostCurrent._img_bomb.getBitmap());
 }else {
 //BA.debugLineNum = 191;BA.debugLine="fundos(idx).Bitmap = img_vazio.Bitmap";
mostCurrent._fundos[_idx].setBitmap(mostCurrent._img_vazio.getBitmap());
 };
 //BA.debugLineNum = 194;BA.debugLine="If tipos(idx) = 0 And count > 0 Then";
if (_tipos[_idx]==0 && _count>0) { 
 //BA.debugLineNum = 195;BA.debugLine="lbls(idx).Text = count";
mostCurrent._lbls[_idx].setText(BA.ObjectToCharSequence(_count));
 //BA.debugLineNum = 196;BA.debugLine="Select count";
switch (_count) {
case 1: {
 //BA.debugLineNum = 197;BA.debugLine="Case 1: lbls(idx).TextColor = Colors.Blue";
mostCurrent._lbls[_idx].setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Blue);
 break; }
case 2: {
 //BA.debugLineNum = 198;BA.debugLine="Case 2: lbls(idx).TextColor = Colors.Green";
mostCurrent._lbls[_idx].setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Green);
 break; }
case 3: {
 //BA.debugLineNum = 199;BA.debugLine="Case 3: lbls(idx).TextColor = Colors.Red";
mostCurrent._lbls[_idx].setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Red);
 break; }
case 4: {
 //BA.debugLineNum = 200;BA.debugLine="Case 4: lbls(idx).TextColor = Colors.RGB(114, 4";
mostCurrent._lbls[_idx].setTextColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (114),(int) (47),(int) (55)));
 break; }
case 5: {
 //BA.debugLineNum = 201;BA.debugLine="Case 5: lbls(idx).TextColor = Colors.RGB(88, 41";
mostCurrent._lbls[_idx].setTextColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (88),(int) (41),(int) (65)));
 break; }
default: {
 //BA.debugLineNum = 202;BA.debugLine="Case Else: lbls(idx).TextColor = Colors.Black";
mostCurrent._lbls[_idx].setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 break; }
}
;
 }else {
 //BA.debugLineNum = 205;BA.debugLine="lbls(idx).Text = \"\"";
mostCurrent._lbls[_idx].setText(BA.ObjectToCharSequence(""));
 };
 //BA.debugLineNum = 207;BA.debugLine="End Sub";
return "";
}
public static String  _bt_action_click() throws Exception{
anywheresoftware.b4a.objects.ButtonWrapper _btn = null;
int _idx = 0;
 //BA.debugLineNum = 244;BA.debugLine="Sub bt_action_Click";
 //BA.debugLineNum = 245;BA.debugLine="Dim btn As Button = Sender";
_btn = new anywheresoftware.b4a.objects.ButtonWrapper();
_btn = (anywheresoftware.b4a.objects.ButtonWrapper) anywheresoftware.b4a.AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.objects.ButtonWrapper(), (android.widget.Button)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 246;BA.debugLine="Dim idx As Int = btn.Tag";
_idx = (int)(BA.ObjectToNumber(_btn.getTag()));
 //BA.debugLineNum = 248;BA.debugLine="If tmrToque.Enabled And idx = idxPendente Then";
if (_tmrtoque.getEnabled() && _idx==_idxpendente) { 
 //BA.debugLineNum = 249;BA.debugLine="tmrToque.Enabled = False";
_tmrtoque.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 250;BA.debugLine="If tipos(idx) = 1 Then GameOver(btn) Else Revela";
if (_tipos[_idx]==1) { 
_gameover(_btn);}
else {
_revelar(_idx);};
 }else {
 //BA.debugLineNum = 252;BA.debugLine="idxPendente = idx";
_idxpendente = _idx;
 //BA.debugLineNum = 253;BA.debugLine="tmrToque.Enabled = True";
_tmrtoque.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 255;BA.debugLine="End Sub";
return "";
}
public static String  _bt_ini_click() throws Exception{
 //BA.debugLineNum = 76;BA.debugLine="Sub bt_ini_Click";
 //BA.debugLineNum = 77;BA.debugLine="lbl_info.Visible = False";
mostCurrent._lbl_info.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 78;BA.debugLine="If bt_ini.Text = \"Iniciar\" Or bt_ini.Text = \"Joga";
if ((mostCurrent._bt_ini.getText()).equals("Iniciar") || (mostCurrent._bt_ini.getText()).equals("Jogar novamente")) { 
 //BA.debugLineNum = 79;BA.debugLine="IniciarJogo";
_iniciarjogo();
 }else if((mostCurrent._bt_ini.getText()).equals("Pausar")) { 
 //BA.debugLineNum = 81;BA.debugLine="AlternarPausa(True)";
_alternarpausa(anywheresoftware.b4a.keywords.Common.True);
 }else if((mostCurrent._bt_ini.getText()).equals("Continuar")) { 
 //BA.debugLineNum = 83;BA.debugLine="AlternarPausa(False)";
_alternarpausa(anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 85;BA.debugLine="End Sub";
return "";
}
public static int  _contarvizinhos(int _lin,int _col) throws Exception{
int _count = 0;
int _vlin = 0;
int _vcol = 0;
int _vidx = 0;
 //BA.debugLineNum = 157;BA.debugLine="Sub ContarVizinhos(lin As Int, col As Int) As Int";
 //BA.debugLineNum = 158;BA.debugLine="Dim count As Int = 0";
_count = (int) (0);
 //BA.debugLineNum = 159;BA.debugLine="Dim vlin, vcol, vidx As Int";
_vlin = 0;
_vcol = 0;
_vidx = 0;
 //BA.debugLineNum = 160;BA.debugLine="For vlin = lin - 1 To lin + 1";
{
final int step3 = 1;
final int limit3 = (int) (_lin+1);
_vlin = (int) (_lin-1) ;
for (;_vlin <= limit3 ;_vlin = _vlin + step3 ) {
 //BA.debugLineNum = 161;BA.debugLine="For vcol = col - 1 To col + 1";
{
final int step4 = 1;
final int limit4 = (int) (_col+1);
_vcol = (int) (_col-1) ;
for (;_vcol <= limit4 ;_vcol = _vcol + step4 ) {
 //BA.debugLineNum = 162;BA.debugLine="If vlin >= 0 And vlin <= 8 And vcol >= 0 And vc";
if (_vlin>=0 && _vlin<=8 && _vcol>=0 && _vcol<=8) { 
 //BA.debugLineNum = 163;BA.debugLine="If Not(vlin = lin And vcol = col) Then";
if (anywheresoftware.b4a.keywords.Common.Not(_vlin==_lin && _vcol==_col)) { 
 //BA.debugLineNum = 164;BA.debugLine="vidx = GetIndex(vlin, vcol)";
_vidx = _getindex(_vlin,_vcol);
 //BA.debugLineNum = 165;BA.debugLine="If tipos(vidx) = 1 Then count = count + 1";
if (_tipos[_vidx]==1) { 
_count = (int) (_count+1);};
 };
 };
 }
};
 }
};
 //BA.debugLineNum = 170;BA.debugLine="Return count";
if (true) return _count;
 //BA.debugLineNum = 171;BA.debugLine="End Sub";
return 0;
}
public static String  _criargrade() throws Exception{
int _col = 0;
int _lin = 0;
int _idx = 0;
int _topooffset = 0;
anywheresoftware.b4a.objects.ImageViewWrapper _iv = null;
anywheresoftware.b4a.objects.LabelWrapper _lbl = null;
anywheresoftware.b4a.objects.ButtonWrapper _btn = null;
 //BA.debugLineNum = 209;BA.debugLine="Sub CriarGrade 'Fundo (ImageView)| Label (Número d";
 //BA.debugLineNum = 210;BA.debugLine="Dim col, lin, idx As Int";
_col = 0;
_lin = 0;
_idx = 0;
 //BA.debugLineNum = 211;BA.debugLine="Dim topoOffset As Int = celula";
_topooffset = _celula;
 //BA.debugLineNum = 213;BA.debugLine="idx = 0";
_idx = (int) (0);
 //BA.debugLineNum = 214;BA.debugLine="For lin = 0 To 8";
{
final int step4 = 1;
final int limit4 = (int) (8);
_lin = (int) (0) ;
for (;_lin <= limit4 ;_lin = _lin + step4 ) {
 //BA.debugLineNum = 215;BA.debugLine="For col = 0 To 8";
{
final int step5 = 1;
final int limit5 = (int) (8);
_col = (int) (0) ;
for (;_col <= limit5 ;_col = _col + step5 ) {
 //BA.debugLineNum = 217;BA.debugLine="Dim iv As ImageView";
_iv = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 218;BA.debugLine="iv.Initialize(\"fundo_ev\")";
_iv.Initialize(mostCurrent.activityBA,"fundo_ev");
 //BA.debugLineNum = 219;BA.debugLine="Activity.AddView(iv, col * celula + pad, topoOf";
mostCurrent._activity.AddView((android.view.View)(_iv.getObject()),(int) (_col*_celula+_pad),(int) (_topooffset+_lin*_celula+_pad),(int) (_celula-_pad*2),(int) (_celula-_pad*2));
 //BA.debugLineNum = 220;BA.debugLine="fundos(idx) = iv";
mostCurrent._fundos[_idx] = _iv;
 //BA.debugLineNum = 223;BA.debugLine="Dim lbl As Label";
_lbl = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 224;BA.debugLine="lbl.Initialize(\"\")";
_lbl.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 225;BA.debugLine="lbl.TextSize = 16";
_lbl.setTextSize((float) (16));
 //BA.debugLineNum = 226;BA.debugLine="lbl.Typeface = Typeface.DEFAULT_BOLD";
_lbl.setTypeface(anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT_BOLD);
 //BA.debugLineNum = 227;BA.debugLine="lbl.Gravity = Gravity.CENTER";
_lbl.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 228;BA.debugLine="Activity.AddView(lbl, col * celula + pad, topoO";
mostCurrent._activity.AddView((android.view.View)(_lbl.getObject()),(int) (_col*_celula+_pad),(int) (_topooffset+_lin*_celula+_pad),(int) (_celula-_pad*2),(int) (_celula-_pad*2));
 //BA.debugLineNum = 229;BA.debugLine="lbls(idx) = lbl";
mostCurrent._lbls[_idx] = _lbl;
 //BA.debugLineNum = 232;BA.debugLine="Dim btn As Button";
_btn = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 233;BA.debugLine="btn.Initialize(\"bt_action\")";
_btn.Initialize(mostCurrent.activityBA,"bt_action");
 //BA.debugLineNum = 234;BA.debugLine="btn.Tag = idx";
_btn.setTag((Object)(_idx));
 //BA.debugLineNum = 235;BA.debugLine="btns(idx) = btn";
mostCurrent._btns[_idx] = _btn;
 //BA.debugLineNum = 236;BA.debugLine="Activity.AddView(btn, col * celula, topoOffset";
mostCurrent._activity.AddView((android.view.View)(_btn.getObject()),(int) (_col*_celula),(int) (_topooffset+_lin*_celula),_celula,_celula);
 //BA.debugLineNum = 237;BA.debugLine="btns(idx).Visible = visivel 'teste";
mostCurrent._btns[_idx].setVisible(_visivel);
 //BA.debugLineNum = 238;BA.debugLine="idx = idx + 1";
_idx = (int) (_idx+1);
 }
};
 }
};
 //BA.debugLineNum = 241;BA.debugLine="visivel = True";
_visivel = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 242;BA.debugLine="End Sub";
return "";
}
public static String  _finalizarpartida(String _textobotao) throws Exception{
int _n = 0;
 //BA.debugLineNum = 300;BA.debugLine="Sub FinalizarPartida(TextoBotao As String)";
 //BA.debugLineNum = 301;BA.debugLine="tmrJogo.Enabled = False";
_tmrjogo.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 302;BA.debugLine="tmrToque.Enabled = False";
_tmrtoque.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 303;BA.debugLine="jogoIniciado = False";
_jogoiniciado = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 304;BA.debugLine="For n = 0 To 80";
{
final int step4 = 1;
final int limit4 = (int) (80);
_n = (int) (0) ;
for (;_n <= limit4 ;_n = _n + step4 ) {
 //BA.debugLineNum = 305;BA.debugLine="btns(n).Enabled = False";
mostCurrent._btns[_n].setEnabled(anywheresoftware.b4a.keywords.Common.False);
 }
};
 //BA.debugLineNum = 307;BA.debugLine="bt_ini.Text = TextoBotao";
mostCurrent._bt_ini.setText(BA.ObjectToCharSequence(_textobotao));
 //BA.debugLineNum = 308;BA.debugLine="End Sub";
return "";
}
public static String  _flag_ev_click() throws Exception{
anywheresoftware.b4a.objects.ImageViewWrapper _iv = null;
anywheresoftware.b4a.objects.ButtonWrapper _btn = null;
int _idx = 0;
anywheresoftware.b4a.objects.ImageViewWrapper _nobomb = null;
 //BA.debugLineNum = 343;BA.debugLine="Sub flag_ev_Click";
 //BA.debugLineNum = 344;BA.debugLine="Dim iv As ImageView = Sender";
_iv = new anywheresoftware.b4a.objects.ImageViewWrapper();
_iv = (anywheresoftware.b4a.objects.ImageViewWrapper) anywheresoftware.b4a.AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.objects.ImageViewWrapper(), (android.widget.ImageView)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 345;BA.debugLine="Dim btn As Button = iv.Tag";
_btn = new anywheresoftware.b4a.objects.ButtonWrapper();
_btn = (anywheresoftware.b4a.objects.ButtonWrapper) anywheresoftware.b4a.AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.objects.ButtonWrapper(), (android.widget.Button)(_iv.getTag()));
 //BA.debugLineNum = 346;BA.debugLine="Dim idx As Int = btn.Tag";
_idx = (int)(BA.ObjectToNumber(_btn.getTag()));
 //BA.debugLineNum = 348;BA.debugLine="If tipos(idx) = 1 Then bombardasMarcadas = bombar";
if (_tipos[_idx]==1) { 
_bombardasmarcadas = (int) (_bombardasmarcadas-1);};
 //BA.debugLineNum = 349;BA.debugLine="totalBandeiras = totalBandeiras - 1";
_totalbandeiras = (int) (_totalbandeiras-1);
 //BA.debugLineNum = 350;BA.debugLine="lbl_bandeira.Text = totalBandeiras";
mostCurrent._lbl_bandeira.setText(BA.ObjectToCharSequence(_totalbandeiras));
 //BA.debugLineNum = 352;BA.debugLine="Dim nobomb As ImageView";
_nobomb = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 353;BA.debugLine="nobomb.Initialize(\"nobomb_ev\")";
_nobomb.Initialize(mostCurrent.activityBA,"nobomb_ev");
 //BA.debugLineNum = 354;BA.debugLine="nobomb.Bitmap = img_nobomb.Bitmap";
_nobomb.setBitmap(mostCurrent._img_nobomb.getBitmap());
 //BA.debugLineNum = 355;BA.debugLine="nobomb.Tag = iv.Tag";
_nobomb.setTag(_iv.getTag());
 //BA.debugLineNum = 356;BA.debugLine="Activity.AddView(nobomb, iv.Left, iv.Top, celula";
mostCurrent._activity.AddView((android.view.View)(_nobomb.getObject()),_iv.getLeft(),_iv.getTop(),(int) (_celula-_pad*2),(int) (_celula-_pad*2));
 //BA.debugLineNum = 357;BA.debugLine="viewsDinamicas.Add(nobomb)";
mostCurrent._viewsdinamicas.Add((Object)(_nobomb.getObject()));
 //BA.debugLineNum = 359;BA.debugLine="viewsDinamicas.RemoveAt(viewsDinamicas.IndexOf(iv";
mostCurrent._viewsdinamicas.RemoveAt(mostCurrent._viewsdinamicas.IndexOf((Object)(_iv.getObject())));
 //BA.debugLineNum = 360;BA.debugLine="iv.RemoveView";
_iv.RemoveView();
 //BA.debugLineNum = 361;BA.debugLine="End Sub";
return "";
}
public static String  _formatartempo(int _totalsegundos) throws Exception{
int _mins = 0;
int _seg = 0;
 //BA.debugLineNum = 151;BA.debugLine="Sub FormatarTempo(totalSegundos As Int) As String";
 //BA.debugLineNum = 152;BA.debugLine="Dim mins As Int = totalSegundos / 60";
_mins = (int) (_totalsegundos/(double)60);
 //BA.debugLineNum = 153;BA.debugLine="Dim seg As Int = totalSegundos Mod 60";
_seg = (int) (_totalsegundos%60);
 //BA.debugLineNum = 154;BA.debugLine="Return NumberFormat(mins, 2, 0) & \":\" & NumberFor";
if (true) return anywheresoftware.b4a.keywords.Common.NumberFormat(_mins,(int) (2),(int) (0))+":"+anywheresoftware.b4a.keywords.Common.NumberFormat(_seg,(int) (2),(int) (0));
 //BA.debugLineNum = 155;BA.debugLine="End Sub";
return "";
}
public static String  _gameover(anywheresoftware.b4a.objects.ButtonWrapper _btnclicado) throws Exception{
anywheresoftware.b4a.objects.ImageViewWrapper _explod = null;
int _n = 0;
 //BA.debugLineNum = 284;BA.debugLine="Sub GameOver(btnClicado As Button)";
 //BA.debugLineNum = 285;BA.debugLine="btnClicado.Visible = False";
_btnclicado.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 286;BA.debugLine="Dim explod As ImageView";
_explod = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 287;BA.debugLine="explod.Initialize(\"\")";
_explod.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 288;BA.debugLine="explod.Bitmap = img_explod.Bitmap";
_explod.setBitmap(mostCurrent._img_explod.getBitmap());
 //BA.debugLineNum = 289;BA.debugLine="Activity.AddView(explod, btnClicado.Left + pad, b";
mostCurrent._activity.AddView((android.view.View)(_explod.getObject()),(int) (_btnclicado.getLeft()+_pad),(int) (_btnclicado.getTop()+_pad),(int) (_celula-_pad*2),(int) (_celula-_pad*2));
 //BA.debugLineNum = 290;BA.debugLine="viewsDinamicas.Add(explod)";
mostCurrent._viewsdinamicas.Add((Object)(_explod.getObject()));
 //BA.debugLineNum = 293;BA.debugLine="For n = 0 To 80";
{
final int step7 = 1;
final int limit7 = (int) (80);
_n = (int) (0) ;
for (;_n <= limit7 ;_n = _n + step7 ) {
 //BA.debugLineNum = 294;BA.debugLine="If tipos(n) = 1 And btns(n).Visible = True Then";
if (_tipos[_n]==1 && mostCurrent._btns[_n].getVisible()==anywheresoftware.b4a.keywords.Common.True) { 
mostCurrent._btns[_n].setVisible(anywheresoftware.b4a.keywords.Common.False);};
 }
};
 //BA.debugLineNum = 296;BA.debugLine="FinalizarPartida(\"Jogar novamente\")";
_finalizarpartida("Jogar novamente");
 //BA.debugLineNum = 297;BA.debugLine="End Sub";
return "";
}
public static int  _getcol(int _idx) throws Exception{
 //BA.debugLineNum = 182;BA.debugLine="Sub GetCol(idx As Int) As Int";
 //BA.debugLineNum = 183;BA.debugLine="Return idx Mod 9";
if (true) return (int) (_idx%9);
 //BA.debugLineNum = 184;BA.debugLine="End Sub";
return 0;
}
public static int  _getindex(int _lin,int _col) throws Exception{
 //BA.debugLineNum = 174;BA.debugLine="Sub GetIndex(lin As Int, col As Int) As Int";
 //BA.debugLineNum = 175;BA.debugLine="Return lin * 9 + col";
if (true) return (int) (_lin*9+_col);
 //BA.debugLineNum = 176;BA.debugLine="End Sub";
return 0;
}
public static int  _getrow(int _idx) throws Exception{
 //BA.debugLineNum = 178;BA.debugLine="Sub GetRow(idx As Int) As Int";
 //BA.debugLineNum = 179;BA.debugLine="Return idx / 9";
if (true) return (int) (_idx/(double)9);
 //BA.debugLineNum = 180;BA.debugLine="End Sub";
return 0;
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 20;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 21;BA.debugLine="Private bt_action, bt_ini As Button";
mostCurrent._bt_action = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._bt_ini = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 22;BA.debugLine="Private fundos(81), img_bomb, img_explod, img_fla";
mostCurrent._fundos = new anywheresoftware.b4a.objects.ImageViewWrapper[(int) (81)];
{
int d0 = mostCurrent._fundos.length;
for (int i0 = 0;i0 < d0;i0++) {
mostCurrent._fundos[i0] = new anywheresoftware.b4a.objects.ImageViewWrapper();
}
}
;
mostCurrent._img_bomb = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._img_explod = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._img_flag = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._img_nobomb = new anywheresoftware.b4a.objects.ImageViewWrapper();
mostCurrent._img_vazio = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 23;BA.debugLine="Private btns(81) As Button";
mostCurrent._btns = new anywheresoftware.b4a.objects.ButtonWrapper[(int) (81)];
{
int d0 = mostCurrent._btns.length;
for (int i0 = 0;i0 < d0;i0++) {
mostCurrent._btns[i0] = new anywheresoftware.b4a.objects.ButtonWrapper();
}
}
;
 //BA.debugLineNum = 24;BA.debugLine="Private lbls(81), lbl_bandeira, lbl_time, lbl_inf";
mostCurrent._lbls = new anywheresoftware.b4a.objects.LabelWrapper[(int) (81)];
{
int d0 = mostCurrent._lbls.length;
for (int i0 = 0;i0 < d0;i0++) {
mostCurrent._lbls[i0] = new anywheresoftware.b4a.objects.LabelWrapper();
}
}
;
mostCurrent._lbl_bandeira = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_time = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_info = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 25;BA.debugLine="Private tipos(81) As Int";
_tipos = new int[(int) (81)];
;
 //BA.debugLineNum = 26;BA.debugLine="Private bombardasMarcadas, celula, idxPendente, s";
_bombardasmarcadas = 0;
_celula = 0;
_idxpendente = 0;
_segundos = 0;
_totalbandeiras = 0;
 //BA.debugLineNum = 27;BA.debugLine="Private jogoIniciado, jogoPausado, visivel As Boo";
_jogoiniciado = false;
_jogopausado = false;
_visivel = false;
 //BA.debugLineNum = 28;BA.debugLine="Private viewsDinamicas As List";
mostCurrent._viewsdinamicas = new anywheresoftware.b4a.objects.collections.List();
 //BA.debugLineNum = 29;BA.debugLine="Private pad As Int ' Definido globalmente para ev";
_pad = 0;
 //BA.debugLineNum = 30;BA.debugLine="End Sub";
return "";
}
public static String  _iniciarjogo() throws Exception{
int _idx = 0;
 //BA.debugLineNum = 115;BA.debugLine="Sub IniciarJogo";
 //BA.debugLineNum = 116;BA.debugLine="ResetarEstado";
_resetarestado();
 //BA.debugLineNum = 117;BA.debugLine="SortearBombas";
_sortearbombas();
 //BA.debugLineNum = 119;BA.debugLine="Dim idx As Int";
_idx = 0;
 //BA.debugLineNum = 120;BA.debugLine="For idx = 0 To 80";
{
final int step4 = 1;
final int limit4 = (int) (80);
_idx = (int) (0) ;
for (;_idx <= limit4 ;_idx = _idx + step4 ) {
 //BA.debugLineNum = 121;BA.debugLine="btns(idx).Visible = True";
mostCurrent._btns[_idx].setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 122;BA.debugLine="btns(idx).Enabled = True";
mostCurrent._btns[_idx].setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 125;BA.debugLine="AtualizarVisualCelula(idx, ContarVizinhos(GetRow";
_atualizarvisualcelula(_idx,_contarvizinhos(_getrow(_idx),_getcol(_idx)));
 }
};
 //BA.debugLineNum = 128;BA.debugLine="tmrJogo.Enabled = True";
_tmrjogo.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 129;BA.debugLine="bt_ini.Text = \"Pausar\"";
mostCurrent._bt_ini.setText(BA.ObjectToCharSequence("Pausar"));
 //BA.debugLineNum = 130;BA.debugLine="End Sub";
return "";
}
public static String  _nobomb_ev_click() throws Exception{
anywheresoftware.b4a.objects.ImageViewWrapper _iv = null;
anywheresoftware.b4a.objects.ButtonWrapper _btn = null;
 //BA.debugLineNum = 363;BA.debugLine="Sub nobomb_ev_Click";
 //BA.debugLineNum = 364;BA.debugLine="Dim iv As ImageView = Sender";
_iv = new anywheresoftware.b4a.objects.ImageViewWrapper();
_iv = (anywheresoftware.b4a.objects.ImageViewWrapper) anywheresoftware.b4a.AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.objects.ImageViewWrapper(), (android.widget.ImageView)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 365;BA.debugLine="Dim btn As Button = iv.Tag";
_btn = new anywheresoftware.b4a.objects.ButtonWrapper();
_btn = (anywheresoftware.b4a.objects.ButtonWrapper) anywheresoftware.b4a.AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.objects.ButtonWrapper(), (android.widget.Button)(_iv.getTag()));
 //BA.debugLineNum = 366;BA.debugLine="btn.Visible = True";
_btn.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 367;BA.debugLine="viewsDinamicas.RemoveAt(viewsDinamicas.IndexOf(iv";
mostCurrent._viewsdinamicas.RemoveAt(mostCurrent._viewsdinamicas.IndexOf((Object)(_iv.getObject())));
 //BA.debugLineNum = 368;BA.debugLine="iv.RemoveView";
_iv.RemoveView();
 //BA.debugLineNum = 369;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 14;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 15;BA.debugLine="Private xui As XUI";
_xui = new anywheresoftware.b4a.objects.B4XViewWrapper.XUI();
 //BA.debugLineNum = 16;BA.debugLine="Private tmrToque As Timer 'Usado para distinguir";
_tmrtoque = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 17;BA.debugLine="Private tmrJogo As Timer";
_tmrjogo = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 18;BA.debugLine="End Sub";
return "";
}
public static String  _resetarestado() throws Exception{
anywheresoftware.b4a.objects.ConcreteViewWrapper _v = null;
int _n = 0;
 //BA.debugLineNum = 96;BA.debugLine="Sub ResetarEstado";
 //BA.debugLineNum = 97;BA.debugLine="segundos = 0";
_segundos = (int) (0);
 //BA.debugLineNum = 98;BA.debugLine="lbl_time.Text = \"00:00\"";
mostCurrent._lbl_time.setText(BA.ObjectToCharSequence("00:00"));
 //BA.debugLineNum = 99;BA.debugLine="jogoIniciado = False";
_jogoiniciado = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 100;BA.debugLine="jogoPausado = False";
_jogopausado = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 101;BA.debugLine="bombardasMarcadas = 0";
_bombardasmarcadas = (int) (0);
 //BA.debugLineNum = 102;BA.debugLine="totalBandeiras = 0";
_totalbandeiras = (int) (0);
 //BA.debugLineNum = 103;BA.debugLine="lbl_bandeira.Text = \"0\"";
mostCurrent._lbl_bandeira.setText(BA.ObjectToCharSequence("0"));
 //BA.debugLineNum = 105;BA.debugLine="For Each v As View In viewsDinamicas";
_v = new anywheresoftware.b4a.objects.ConcreteViewWrapper();
{
final anywheresoftware.b4a.BA.IterableList group8 = mostCurrent._viewsdinamicas;
final int groupLen8 = group8.getSize()
;int index8 = 0;
;
for (; index8 < groupLen8;index8++){
_v = (anywheresoftware.b4a.objects.ConcreteViewWrapper) anywheresoftware.b4a.AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.objects.ConcreteViewWrapper(), (android.view.View)(group8.Get(index8)));
 //BA.debugLineNum = 106;BA.debugLine="v.RemoveView";
_v.RemoveView();
 }
};
 //BA.debugLineNum = 108;BA.debugLine="viewsDinamicas.Clear";
mostCurrent._viewsdinamicas.Clear();
 //BA.debugLineNum = 110;BA.debugLine="For n = 0 To 80";
{
final int step12 = 1;
final int limit12 = (int) (80);
_n = (int) (0) ;
for (;_n <= limit12 ;_n = _n + step12 ) {
 //BA.debugLineNum = 111;BA.debugLine="tipos(n) = 0";
_tipos[_n] = (int) (0);
 }
};
 //BA.debugLineNum = 113;BA.debugLine="End Sub";
return "";
}
public static String  _revelar(int _idx) throws Exception{
anywheresoftware.b4a.objects.collections.List _fila = null;
boolean[] _visitados = null;
int _atual = 0;
int _lin = 0;
int _col = 0;
int _vlin = 0;
int _vcol = 0;
int _vidx = 0;
 //BA.debugLineNum = 310;BA.debugLine="Sub Revelar(idx As Int)";
 //BA.debugLineNum = 311;BA.debugLine="Dim fila As List";
_fila = new anywheresoftware.b4a.objects.collections.List();
 //BA.debugLineNum = 312;BA.debugLine="Dim visitados(81) As Boolean";
_visitados = new boolean[(int) (81)];
;
 //BA.debugLineNum = 313;BA.debugLine="Dim atual, lin, col, vlin, vcol, vidx As Int";
_atual = 0;
_lin = 0;
_col = 0;
_vlin = 0;
_vcol = 0;
_vidx = 0;
 //BA.debugLineNum = 315;BA.debugLine="fila.Initialize";
_fila.Initialize();
 //BA.debugLineNum = 316;BA.debugLine="fila.Add(idx)";
_fila.Add((Object)(_idx));
 //BA.debugLineNum = 317;BA.debugLine="visitados(idx) = True";
_visitados[_idx] = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 319;BA.debugLine="Do While fila.Size > 0";
while (_fila.getSize()>0) {
 //BA.debugLineNum = 320;BA.debugLine="atual = fila.Get(0)";
_atual = (int)(BA.ObjectToNumber(_fila.Get((int) (0))));
 //BA.debugLineNum = 321;BA.debugLine="fila.RemoveAt(0)";
_fila.RemoveAt((int) (0));
 //BA.debugLineNum = 322;BA.debugLine="btns(atual).Visible = False";
mostCurrent._btns[_atual].setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 324;BA.debugLine="lin = GetRow(atual)";
_lin = _getrow(_atual);
 //BA.debugLineNum = 325;BA.debugLine="col = GetCol(atual)";
_col = _getcol(_atual);
 //BA.debugLineNum = 327;BA.debugLine="If tipos(atual) = 0 And ContarVizinhos(lin, col)";
if (_tipos[_atual]==0 && _contarvizinhos(_lin,_col)==0) { 
 //BA.debugLineNum = 328;BA.debugLine="For vlin = lin - 1 To lin + 1";
{
final int step14 = 1;
final int limit14 = (int) (_lin+1);
_vlin = (int) (_lin-1) ;
for (;_vlin <= limit14 ;_vlin = _vlin + step14 ) {
 //BA.debugLineNum = 329;BA.debugLine="For vcol = col - 1 To col + 1";
{
final int step15 = 1;
final int limit15 = (int) (_col+1);
_vcol = (int) (_col-1) ;
for (;_vcol <= limit15 ;_vcol = _vcol + step15 ) {
 //BA.debugLineNum = 330;BA.debugLine="If vlin >= 0 And vlin <= 8 And vcol >= 0 And";
if (_vlin>=0 && _vlin<=8 && _vcol>=0 && _vcol<=8) { 
 //BA.debugLineNum = 331;BA.debugLine="vidx = GetIndex(vlin, vcol)";
_vidx = _getindex(_vlin,_vcol);
 //BA.debugLineNum = 332;BA.debugLine="If Not(visitados(vidx)) And tipos(vidx) = 0";
if (anywheresoftware.b4a.keywords.Common.Not(_visitados[_vidx]) && _tipos[_vidx]==0) { 
 //BA.debugLineNum = 333;BA.debugLine="visitados(vidx) = True";
_visitados[_vidx] = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 334;BA.debugLine="fila.Add(vidx)";
_fila.Add((Object)(_vidx));
 };
 };
 }
};
 }
};
 };
 }
;
 //BA.debugLineNum = 341;BA.debugLine="End Sub";
return "";
}
public static String  _sortearbombas() throws Exception{
int _n = 0;
int _sorteados = 0;
 //BA.debugLineNum = 133;BA.debugLine="Sub SortearBombas";
 //BA.debugLineNum = 134;BA.debugLine="Dim n As Int";
_n = 0;
 //BA.debugLineNum = 135;BA.debugLine="Dim sorteados As Int = 0";
_sorteados = (int) (0);
 //BA.debugLineNum = 136;BA.debugLine="Do While sorteados < 10";
while (_sorteados<10) {
 //BA.debugLineNum = 137;BA.debugLine="n = Rnd(0, 81)";
_n = anywheresoftware.b4a.keywords.Common.Rnd((int) (0),(int) (81));
 //BA.debugLineNum = 138;BA.debugLine="If tipos(n) = 0 Then";
if (_tipos[_n]==0) { 
 //BA.debugLineNum = 139;BA.debugLine="tipos(n) = 1";
_tipos[_n] = (int) (1);
 //BA.debugLineNum = 140;BA.debugLine="sorteados = sorteados + 1";
_sorteados = (int) (_sorteados+1);
 };
 }
;
 //BA.debugLineNum = 143;BA.debugLine="End Sub";
return "";
}
public static String  _tmrjogo_tick() throws Exception{
 //BA.debugLineNum = 145;BA.debugLine="Sub tmrJogo_Tick";
 //BA.debugLineNum = 146;BA.debugLine="segundos = segundos + 1";
_segundos = (int) (_segundos+1);
 //BA.debugLineNum = 147;BA.debugLine="lbl_time.Text = FormatarTempo(segundos)";
mostCurrent._lbl_time.setText(BA.ObjectToCharSequence(_formatartempo(_segundos)));
 //BA.debugLineNum = 148;BA.debugLine="End Sub";
return "";
}
public static String  _tmrtoque_tick() throws Exception{
anywheresoftware.b4a.objects.ButtonWrapper _btn = null;
anywheresoftware.b4a.objects.ImageViewWrapper _flag = null;
 //BA.debugLineNum = 257;BA.debugLine="Sub tmrToque_Tick";
 //BA.debugLineNum = 258;BA.debugLine="tmrToque.Enabled = False";
_tmrtoque.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 259;BA.debugLine="If totalBandeiras >= 10 Then Return";
if (_totalbandeiras>=10) { 
if (true) return "";};
 //BA.debugLineNum = 261;BA.debugLine="Dim btn As Button = btns(idxPendente)";
_btn = new anywheresoftware.b4a.objects.ButtonWrapper();
_btn = mostCurrent._btns[_idxpendente];
 //BA.debugLineNum = 262;BA.debugLine="btn.Visible = False";
_btn.setVisible(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 264;BA.debugLine="Dim flag As ImageView";
_flag = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 265;BA.debugLine="flag.Initialize(\"flag_ev\")";
_flag.Initialize(mostCurrent.activityBA,"flag_ev");
 //BA.debugLineNum = 266;BA.debugLine="flag.Bitmap = img_flag.Bitmap";
_flag.setBitmap(mostCurrent._img_flag.getBitmap());
 //BA.debugLineNum = 267;BA.debugLine="flag.Tag = btn";
_flag.setTag((Object)(_btn.getObject()));
 //BA.debugLineNum = 268;BA.debugLine="Activity.AddView(flag, btn.Left + pad, btn.Top +";
mostCurrent._activity.AddView((android.view.View)(_flag.getObject()),(int) (_btn.getLeft()+_pad),(int) (_btn.getTop()+_pad),(int) (_celula-_pad*2),(int) (_celula-_pad*2));
 //BA.debugLineNum = 269;BA.debugLine="viewsDinamicas.Add(flag)";
mostCurrent._viewsdinamicas.Add((Object)(_flag.getObject()));
 //BA.debugLineNum = 271;BA.debugLine="totalBandeiras = totalBandeiras + 1";
_totalbandeiras = (int) (_totalbandeiras+1);
 //BA.debugLineNum = 272;BA.debugLine="lbl_bandeira.Text = totalBandeiras";
mostCurrent._lbl_bandeira.setText(BA.ObjectToCharSequence(_totalbandeiras));
 //BA.debugLineNum = 274;BA.debugLine="If tipos(idxPendente) = 1 Then";
if (_tipos[_idxpendente]==1) { 
 //BA.debugLineNum = 275;BA.debugLine="bombardasMarcadas = bombardasMarcadas + 1";
_bombardasmarcadas = (int) (_bombardasmarcadas+1);
 //BA.debugLineNum = 276;BA.debugLine="If bombardasMarcadas = 10 Then Vitoria";
if (_bombardasmarcadas==10) { 
_vitoria();};
 };
 //BA.debugLineNum = 278;BA.debugLine="End Sub";
return "";
}
public static String  _vitoria() throws Exception{
 //BA.debugLineNum = 280;BA.debugLine="Sub Vitoria";
 //BA.debugLineNum = 281;BA.debugLine="FinalizarPartida(\"Jogar novamente\")";
_finalizarpartida("Jogar novamente");
 //BA.debugLineNum = 282;BA.debugLine="End Sub";
return "";
}
}
