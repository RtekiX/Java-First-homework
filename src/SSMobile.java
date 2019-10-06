import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import jdk.jfr.Unsigned;

public class SSMobile {
	/***********************************
	 * 构造函数
	 ********************************************************/
	static DecimalFormat Std = new DecimalFormat("0.0");// 定义格式化器
	/***********************************
	 * 资费包部分
	 ********************************************************/
	public abstract class ServicePack {
		public double Price; // 资费包的基础价格
		public int Type; // 用于标记资费包种类
		public int TalkTime; // 通话时间 分钟
		public int MessageNum; // 短信条数
		public int Wired; // 流量 MB
		abstract void PrintPack(); // 打印该资费包的具体信息
	}

	public class TalkPack extends ServicePack {
		public void PrintPack() {
			System.out.print("话痨套餐" + '\n' + "通话时间：" + TalkTime + "分钟" + '\n'
					+ "短信条数：" + MessageNum + '\n' + "基础价格：" + Price + "元/月"
					+ '\n');
		} // 打印该资费包的具体信息
		TalkPack() {
			Type = 1;
			TalkTime = 500;
			Wired = 0;
			MessageNum = 30;
			Price = 58;
		}
	}

	public class NetPack extends ServicePack {
		NetPack() {
			TalkTime = 0;
			MessageNum = 0;
			Type = 2;
			Wired = 3072;
			Price = 68;
		}
		public void PrintPack() {
			System.out.print("网虫套餐" + '\n' + "上网流量：" + Wired + "MB" + '\n'
					+ "基础价格：" + Price + "元/月" + '\n');
		} // 打印该资费包的具体信息
	}

	public class SuperPack extends ServicePack {
		SuperPack() {
			Type = 3;
			TalkTime = 200;
			MessageNum = 50;
			Wired = 1024;
			Price = 78;
		}
		public void PrintPack() {
			System.out.print("超人套餐" + '\n' + "通话时间：" + TalkTime + "分钟" + '\n'
					+ "短信条数：" + MessageNum + '\n' + "上网流量：" + Wired + "MB"
					+ '\n' + "基础价格：" + Price + "元/月" + '\n');
		} // 打印该资费包的具体信息
	}

	/*************************************
	 * 单个用户的电话卡
	 ******************************************************/
	public class ConsumInfo {
		String cardNum;
		String consumType; // 消费类型
		int Data; // 消费量
		ConsumInfo(String num, String type, int d) {
			cardNum = num;
			consumType = type;
			Data = d;
		}
	} // 记录消费数据
	public static class MobileCard {
		String CardNumber; // 卡号
		String UserName; // 用户账号
		String Password; // 登录密码
		ServicePack PackType; // 资费包类型
		double Balance; // 余额
		double Consum; // 消费量
		int TalkTime_Use; // 实际通话时间
		int MessageNum_Use; // 实际短信条数
		int Wired_Use; // 实际流量使用量
		MobileCard() {
			Balance = 0;
			Consum = 0;
			TalkTime_Use = 0;
			MessageNum_Use = 0;
			Wired_Use = 0;
			CardNumber = UserName = Password = "";
		} // 新创建的卡内余额和消费量默认为0
		public void ShowBill() {
			System.out.print("您的卡号：" + CardNumber + '\n' + "套餐资费："
					+ PackType.Price + '\n' + "合计：" + Std.format(Consum+PackType.Price) + '\n'
					+ "余额：" + Std.format(Balance) + '\n');
		}
		public void ShowAmount() {
			PackType.PrintPack();
			int restTalk, restNet, restMessage;
			if (TalkTime_Use >= PackType.TalkTime) {
				restTalk = 0;
			} else {
				restTalk = PackType.TalkTime - TalkTime_Use;
			}
			if (Wired_Use >= PackType.Wired) {
				restNet = 0;
			} else {
				restNet = PackType.Wired - Wired_Use;
			}
			if (MessageNum_Use >= PackType.MessageNum) {
				restMessage = 0;
			} else {
				restMessage = PackType.MessageNum - MessageNum_Use;
			}
			System.out.println("套餐标准通话时间(分钟)：" + PackType.TalkTime + "  实际通话时间："
					+ TalkTime_Use + "  剩余通话时间：" + restTalk + '\n' + "套餐标准短信条数："
					+ PackType.MessageNum + "  实际短信条数：" + MessageNum_Use
					+ "  剩余短信条数：" + restMessage + '\n' + "套餐标准上网流量(MB)："
					+ PackType.Wired + "  实际上网流量：" + Wired_Use + "  剩余上网流量："
					+ restNet + '\n');
		} // 打印当前资费信息
	}
	/*******************************************************
	 * 营业大厅主功能部分
	 ********************************************************/
	// 表示网上营业大厅的所有功能
	Map<String, MobileCard> Users = new HashMap<String, MobileCard>(); // 用户账号映射一张手机卡，账号和卡都是唯一的
	Map<String, List<ConsumInfo>> ConsumList = new HashMap<String, List<ConsumInfo>>(); // 保存消费记录的映射
	/***************************************************
	 * 使用嗖嗖的三个业务功能
	 * 
	 * @throws IOException
	 *****************************************************/
	public void send(MobileCard user, int Num) throws IOException {
		if (user.MessageNum_Use < user.PackType.MessageNum) { // 如果还剩有套餐余量
			if (Num > user.PackType.MessageNum - user.MessageNum_Use) { // 如果要发送的量大于剩余量
				if (user.Balance < (Num
						- (user.PackType.MessageNum - user.MessageNum_Use)
								* 0.1)) {
					System.out.println("您的余额不足进行此次消费，请充值后再使用");
					return;
				} else {
					user.Balance -= (Num
							- (user.PackType.MessageNum - user.MessageNum_Use))
							* 0.1;
					user.Consum += (Num
							- (user.PackType.MessageNum - user.MessageNum_Use))
							* 0.1;
					user.MessageNum_Use += Num;
				}
			} else {
				user.MessageNum_Use += Num;
			} // 如果剩余量足够，那么不扣余额
		} else {
			if (user.Balance < Num * 0.1) {
				System.out.println("您的余额不足进行此次消费，请充值后再使用");
				return;
			} else {
				user.Balance -= Num * 0.1;
				user.Consum += Num * 0.1;
				user.MessageNum_Use += Num;
			}
		} // 如果没有套餐余量
		ConsumInfo NewInfo = new ConsumInfo(user.CardNumber, "短信", Num);
		ConsumList.get(user.CardNumber).add(NewInfo); // 在当前用户的消费列表里加入一条消费信息
		OutPutInfo(NewInfo);
	} // 输入发送数量，本月短信数增加
	public void call(MobileCard user, int CallTime) throws IOException {
		if (user.TalkTime_Use < user.PackType.TalkTime) { // 如果还剩有套餐余量
			if (CallTime > user.PackType.TalkTime - user.TalkTime_Use) { // 如果要发送的量大于剩余量
				if (user.Balance < (CallTime
						- (user.PackType.TalkTime - user.TalkTime_Use)
								* 0.2)) {
					System.out.println("您的余额不足进行此次消费，请充值后再使用");
					return;
				} else {
					user.Balance -= (CallTime
							- (user.PackType.TalkTime - user.TalkTime_Use))
							* 0.2;
					user.Consum += (CallTime
							- (user.PackType.TalkTime - user.TalkTime_Use))
							* 0.2;
					user.TalkTime_Use += CallTime;
				}
			} else {
				user.TalkTime_Use += CallTime;
			} // 如果剩余量足够，那么不扣余额
		} else {
			if (user.Balance < CallTime * 0.2) {
				System.out.println("您的余额不足进行此次消费，请充值后再使用");
				return;
			} else {
				user.Balance -= CallTime * 0.2;
				user.Consum += CallTime * 0.2;
				user.TalkTime_Use += CallTime;
			}
		} // 如果没有套餐余量
		ConsumInfo NewInfo = new ConsumInfo(user.CardNumber, "通话", CallTime);
		ConsumList.get(user.CardNumber).add(NewInfo); // 在当前用户的消费列表里加入一条消费信息
		OutPutInfo(NewInfo);
	} // 输入通话时间，总通话时间增加
	public void net(MobileCard user, int WiredUse) throws IOException {
		if (user.Wired_Use < user.PackType.Wired) { // 如果还剩有套餐余量
			if (WiredUse > user.PackType.Wired - user.Wired_Use) { // 如果要发送的量大于剩余量
				if (user.Balance < (WiredUse
						- (user.PackType.Wired - user.Wired_Use)
								* 0.1)) {
					System.out.println("您的余额不足进行此次消费，请充值后再使用");
					return;
				} else {
					user.Balance -= (WiredUse
							- (user.PackType.Wired - user.Wired_Use))
							* 0.1;
					user.Consum += (WiredUse
							- (user.PackType.Wired - user.Wired_Use))
							* 0.1;
					user.Wired_Use += WiredUse;
				}
			} else {
				user.Wired_Use += WiredUse;
			} // 如果剩余量足够，那么不扣余额
		} else {
			if (user.Balance < WiredUse * 0.1) {
				System.out.println("您的余额不足进行此次消费，请充值后再使用");
				return;
			} else {
				user.Balance -= WiredUse * 0.1;
				user.Consum += WiredUse * 0.1;
				user.Wired_Use += WiredUse;
			}
		} // 如果没有套餐余量
		ConsumInfo NewInfo = new ConsumInfo(user.CardNumber, "上网", WiredUse);
		ConsumList.get(user.CardNumber).add(NewInfo); // 在当前用户的消费列表里加入一条消费信息
		OutPutInfo(NewInfo);
	} // 上网，流量使用增加
	/**********************************************
	 * 卡号随机生成，以及创建、删除电话卡的功能
	 ************************************************/
	String GetRandomNumber() {
		String CardNumber = Rand();
		while (IsExistNumber(CardNumber)) {
			CardNumber = Rand();
		}
		return CardNumber;
	} // 分配一个新卡号
	String Rand() {
		String temp = "139";
		Random rand = new Random();
		for (int i = 0; i < 8; i++) {
			temp += rand.nextInt(10);
		}
		return temp;
	} // 卡号随机生成器
	public boolean IsExistNumber(String CardNumber) {
		for (MobileCard value : Users.values()) {
			if (value.CardNumber.equals(CardNumber)) {
				return true;
			}
		}
		return false;
	} // 检查是否有相同卡号的卡
	public boolean IsExistUser(String UserName) {
		if (Users.containsKey(UserName)) {
			return true;
		}
		return false;
	} // 检查是否有相同用户名的卡
	public void CreatNewAccount() {
		Scanner read = new Scanner(System.in);
		MobileCard NewCard = new MobileCard();
		String temp;
		System.out.print("欢迎注册新手机卡" + '\n' + "请输入用户账号：");
		while (true) {
			temp = read.nextLine();
			if (IsExistUser(temp)) {
				System.out.println("用户名已存在，请输入其他用户名。");
			} else {
				NewCard.UserName = temp;
				break;
			}
		}
		System.out.print("请输入用户密码：");
		NewCard.Password = read.nextLine();
		String NewNumber[] = new String[9];
		for (int i = 0; i < 9; i++) {
			NewNumber[i] = GetRandomNumber();
		}
		System.out.println("请选择一个卡号：");
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				System.out.print(NewNumber[3 * i + j] + " ");
			}
			System.out.print('\n');
		}
		System.out.print("请选择一个电话号码（1~9）：");
		NewCard.CardNumber = NewNumber[read.nextInt() - 1];
		System.out.print("请选择您的资费套餐：1.话痨套餐：通话时长500分钟，短信条数30条，资费58元/月" + '\n'
				+ "2.网虫套餐：上网流量3GB，资费68元/月" + '\n'
				+ "3.超人套餐：通话时长200分钟，短信条数50条，上网流量1GB，资费78元/月" + '\n');
		int tempNum = read.nextInt();
		double tempBal;
		while (tempNum != 1 && tempNum != 2 && tempNum != 3) {
			System.out.println("请输入1-3的数字来选择套餐。");
			tempNum = read.nextInt();
		}
		System.out.print("请输入预存金额，预存金额应不小于当前套餐月租费用：");
		tempBal = read.nextDouble();
		if (tempNum == 1) {
			NewCard.PackType = new TalkPack();
			while (tempBal < 58) {
				System.out.println("请预存至少58元！");
				tempBal = read.nextDouble();
			}
		} else if (tempNum == 2) {
			NewCard.PackType = new NetPack();
			while (tempBal < 68) {
				System.out.println("请预存至少68元！");
				tempBal = read.nextDouble();
			}
		} else if (tempNum == 3) {
			NewCard.PackType = new SuperPack();
			while (tempBal < 78) {
				System.out.println("请预存至少78元！");
				tempBal = read.nextDouble();
			}
		}
		NewCard.Balance += (tempBal - NewCard.PackType.Price);
		Users.put(NewCard.UserName, NewCard); // 将这张卡添加至User名单中
		List<ConsumInfo> newList = new ArrayList<ConsumInfo>();
		ConsumList.put(NewCard.CardNumber, newList);
		System.out.println("注册成功，您的卡号是：" + NewCard.CardNumber + "  用户名："
				+ NewCard.UserName + "  当前余额：" + NewCard.Balance);
		NewCard.PackType.PrintPack();

	} // 创建一个新的账号（电话卡）
	public void DelCard(String UserName) {
		Scanner read = new Scanner(System.in);
		System.out.print("请再次确认是否要办理退网，确认1，输入其他数字取消：");
		int sel = read.nextInt();
		if (sel != 1) {
			System.out.println("取消办理退网，回到上一级菜单");
			return;
		}
		System.out
				.println("卡号" + Users.get(UserName).CardNumber + "办理退网成功，感谢使用");
		Users.remove(UserName);
	} // 删除指定电话卡及其用户名
	public void Recharge() {
		Scanner read = new Scanner(System.in);
		String temp;
		MobileCard pCard = new MobileCard();
		double charge;
		System.out.print("请输入你要充值的电话卡号：");
		temp = read.nextLine();
		if (temp.length() != 11) {
			System.out.println("输入错误，电话号码的位数应为11位。");
		} else if (!IsExistNumber(temp)) {
			System.out.println("不存在此电话号码。");
		} else {
			System.out.print("请输入你要充值的金额：");
			charge = read.nextDouble();
			if(charge<=0) {
				System.out.println("充值金额必须为正数！");
				return;
			}
			for (MobileCard value : Users.values()) {
				if (value.CardNumber.equals(temp)) {
					pCard = value;
				}
			}
			pCard.Balance += charge;
			System.out.print("充值成功，您的余额为：" + pCard.Balance + '\n');
		}
	} // 为某个电话卡充值
	/*************************************
	 * 登陆系统的功能
	 *******************************************/
	public void Login() {
		Scanner read = new Scanner(System.in);
		System.out.print("请输入用户名：");
		String tempUsr = read.nextLine();
		String tempPsw;
		if (!IsExistUser(tempUsr)) {
			System.out.println("不存在该用户！");
			return;
		} else {
			System.out.print("请输入密码：");
			tempPsw = read.nextLine();
			if (!Users.get(tempUsr).Password.equals(tempPsw)) {
				System.out.println("密码错误！");
				return;
			} else {
				while (true) {
					MobileCard p = Users.get(tempUsr);
					System.out.print("1.本月账单查询" + '\n' + "2.套餐余量查询" + '\n'
							+ "3.打印消费详单" + '\n' + "4.套餐变更" + '\n' + "5.办理退网"
							+ "请选择(1-5选择功能，其他键返回上一级)");
					int sel = read.nextInt();
					switch (sel) {
						case (1) : {
							p.ShowBill();
						}
							break;
						case (2) : {
							p.ShowAmount();
						}
							break;
						case (3) : {
							PrintUserConsum(p.CardNumber);
						}
							break;
						case (4) : {
							PackChange(p);
						}
							break;
						case (5) : {
							DelCard(p.UserName);
							return;
						}
						default : {

							return;
						}
					} // 输入1-5选择功能，输入其他字符跳出循环
				}
			}
		}
	} // 登录系统后的二级菜单
	public void PrintUserConsum(String CardNum) {
		List<ConsumInfo> pList = ConsumList.get(CardNum);
		if (pList == null) {
			System.out.println("暂无消费记录");
			return;
		}
		System.out.println("******" + CardNum + "消费记录******" + '\n'
				+ "序号      类型      数据(通话(分钟)/短信(条)/上网流量(MB))");
		for (int i = 0; i < pList.size(); i++) {
			System.out.println(i + ".     " + pList.get(i).consumType + "      "
					+ pList.get(i).Data);
		}
	} // 打印某个用户的消费详单
	public void PackChange(MobileCard p) {
		Scanner read = new Scanner(System.in);
		System.out.println("您现在的套餐序号是：" + p.PackType.Type);
		System.out.print("请选择您想更换的资费套餐：1.话痨套餐：通话时长500分钟，短信条数30条，资费58元/月" + '\n'
				+ "2.网虫套餐：上网流量3GB，资费68元/月" + '\n'
				+ "3.超人套餐：通话时长200分钟，短信条数50条，上网流量1GB，资费78元/月" + '\n');
		int sel = read.nextInt();
		if (sel == p.PackType.Type) {
			System.out.println("您现在已经是该套餐，无需更换");

			return;
		} else {
			if (sel == 1) {
				if (p.Balance < 58) {
					System.out.println("您的余额不足以支付新套餐费用，请先充值。当前余额：" + p.Balance);
				} else {
					p.PackType = new TalkPack();
					p.TalkTime_Use = p.Wired_Use = p.MessageNum_Use = 0;
					p.Consum = 0;
					p.Balance -= 58;
					ConsumList.get(p).clear(); // 变更过后同时清除消费数据
					System.out.println("您已经成功变更为话痨套餐！当前余额：" + p.Balance);
				}
			} else if (sel == 2) {
				if (p.Balance < 68) {
					System.out.println("您的余额不足以支付新套餐费用，请先充值。当前余额：" + p.Balance);
				} else {
					p.PackType = new NetPack();
					p.TalkTime_Use = p.Wired_Use = p.MessageNum_Use = 0;
					p.Consum = 0;
					p.Balance -= 68;
					ConsumList.get(p).clear();
					System.out.println("您已经成功变更为网虫套餐！当前余额：" + p.Balance);
				}
			} else if (sel == 3) {
				if (p.Balance < 78) {
					System.out.println("您的余额不足以支付新套餐费用，请先充值。当前余额：" + p.Balance);
				} else {
					p.PackType = new SuperPack();
					p.TalkTime_Use = p.Wired_Use = p.MessageNum_Use = 0;
					p.Consum = 0;
					p.Balance -= 78;
					ConsumList.get(p).clear();
					System.out.println("您已经成功变更为话痨套餐！当前余额：" + p.Balance);
				}
			}
		}
	} // 变更当前用户的套餐
	/*****************************************
	 * 模拟使用嗖嗖
	 ************************************************/
	public class Scene {
		String Type; // 消费类型
		String Description; // 场景描述
		int Data; // 消费量
		Scene(String typeString, String dString, int data) {
			Type = typeString;
			Description = dString;
			Data = data;
		}
	} // 模拟场景
	public void UseSimulator() throws IOException {
		Random r = new Random();
		Scanner read = new Scanner(System.in);
		System.out.print("请输入你的电话号码：");
		String CardNumber = read.nextLine();
		MobileCard pCard = new MobileCard();
		for (MobileCard value : Users.values()) {
			if (value.CardNumber.equals(CardNumber)) {
				pCard = value;
				break;
			}
		} // 找到使用的电话卡
		if (pCard.CardNumber == "") {
			System.out.println("不存在该电话号码，请确认您是否输入正确");
			return;
		} // 如果不存在该卡，则退出
		Scene Test1 = new Scene("通话", "与客户周旋", 90);
		Scene Test2 = new Scene("短信", "过生日邀请朋友聚会", 20);
		Scene Test3 = new Scene("上网", "观看一个五分钟的视频", 132);
		Scene Test4 = new Scene("通话", "周末和家人通话", 30);
		Scene Test5 = new Scene("短信", "向全班同学发布通知", 55);
		Scene Test6 = new Scene("上网", "更新手机中的应用", 554);
		List<Scene> sceneliList = new ArrayList<Scene>();
		sceneliList.add(Test1);
		sceneliList.add(Test2);
		sceneliList.add(Test3);
		sceneliList.add(Test4);
		sceneliList.add(Test5);
		sceneliList.add(Test6);
		int sel = r.nextInt(6); // 随机选择一个场景
		while (true) {
			if ((sel == 0 || sel == 1 || sel == 3 || sel == 4)
					&& pCard.PackType.Type == 2) {
				sel = r.nextInt(6);
				continue;
			} // 网虫套餐无法进行通话和发短信
			if ((sel == 2 || sel == 5) && pCard.PackType.Type == 1) {
				sel = r.nextInt(6);
				continue;
			} // 话痨套餐无法上网
			switch (sel) {
				case (0) : {
					System.out.println(sceneliList.get(sel).Description + ", "
							+ sceneliList.get(sel).Type
							+ sceneliList.get(sel).Data + "分钟");
					call(pCard, sceneliList.get(sel).Data);
					return;
				}
				case (1) : {
					System.out.println(sceneliList.get(sel).Description + ", 发送"
							+ sceneliList.get(sel).Type
							+ sceneliList.get(sel).Data + "条");
					send(pCard, sceneliList.get(sel).Data);
					return;
				}
				case (2) : {
					System.out.println(sceneliList.get(sel).Description + ", "
							+ sceneliList.get(sel).Type + "消耗流量"
							+ sceneliList.get(sel).Data + "MB");
					net(pCard, sceneliList.get(sel).Data);
					return;
				}
				case (3) : {
					System.out.println(sceneliList.get(sel).Description + ", "
							+ sceneliList.get(sel).Type
							+ sceneliList.get(sel).Data + "分钟");
					call(pCard, sceneliList.get(sel).Data);
					return;
				}
				case (4) : {
					System.out.println(sceneliList.get(sel).Description + ", 发送"
							+ sceneliList.get(sel).Type
							+ sceneliList.get(sel).Data + "条");
					send(pCard, sceneliList.get(sel).Data);
					return;
				}
				case (5) : {
					System.out.println(sceneliList.get(sel).Description + ", "
							+ sceneliList.get(sel).Type + "消耗流量"
							+ sceneliList.get(sel).Data + "MB");
					net(pCard, sceneliList.get(sel).Data);
					return;
				}
			} // 模拟六个场景
		} // 如果不支持消费，则继续随机场景；如果支持消费类型，则进行消费并退出
	} // 输入卡号，用随机数来选择一个场景进行消费。
	public void PackInfo() throws IOException {
		String Path = "D:/NAVI/java/SSMobile/src/PackInfo.txt";
		FileInputStream fin = new FileInputStream(Path);
		InputStreamReader reader = new InputStreamReader(fin);
		BufferedReader buffReader = new BufferedReader(reader);
		String strTmp = "";
		while ((strTmp = buffReader.readLine()) != null) {
			System.out.println(strTmp);
		}
		buffReader.close();
	} // 输出资费信息
	public void OutPutInfo(ConsumInfo NewInfo) throws IOException {
		String str = new String();
		str = NewInfo.cardNum + " " + NewInfo.consumType + " " + NewInfo.Data;
		String Path = "D:/NAVI/java/SSMobile/src/ConsumInfo.txt";
		FileOutputStream fout = new FileOutputStream(Path, true);
		OutputStreamWriter writer = new OutputStreamWriter(fout);
		BufferedWriter bufferedWriter = new BufferedWriter(writer);
		bufferedWriter.write(str + '\n');
		bufferedWriter.close();
	} // 将资费输入到文本中
	public static void main(String args[]) throws Exception {
		SSMobile test = new SSMobile();
		Scanner readsScanner = new Scanner(System.in);
		int sel;
		do {
			System.out.print("**********欢迎使用嗖嗖移动业务大厅***********" + '\n'
					+ "1.用户登录  2.用户注册  3.使用嗖嗖  4.话费充值  5.资费说明  6.退出系统" + '\n'
					+ '\n' + "请选择：");
			sel = readsScanner.nextInt();
			if (sel == 6) {
				System.out.println("已退出系統，感謝使用。");
				break;
			} else {
				switch (sel) {
					case (1) : {
						test.Login();
					}
						break;
					case (2) : {
						test.CreatNewAccount();
					}
						break;
					case (3) : {
						test.UseSimulator();
					}
						break;
					case (4) : {
						test.Recharge();
					}
						break;
					case (5) : {
						test.PackInfo();
					}
						break;
					default : {
						continue;
					}
				}
			}
		} while (true);
	}
}
