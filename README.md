# 💸 MoneyMap  

## Your personal finance companion, designed to give you a clear overview of your income and expenses  

## 👩🏻‍💻 Team Members  
- [Popeangă Antonia](https://github.com/antoniapopeanga)  
- [Popa Jasmine](https://github.com/jasminepopa3)  

## 📊 Epic story
MoneyMap simplifies personal finance management by providing users with a clear and intuitive way to track their expenses, monitor income, and gain valuable insights into their spending habits. With seamless data synchronization and real-time updates, it empowers users to take control of their financial future with confidence.  

---  

## 🔍 Preview  

<div style="display: flex; justify-content: space-between;">
    <img src="home_moneymap.jpg" alt="Bug 3" height="450" style="width: 19%;">
    <img src="categories_moneymap.jpg" alt="Bug 4" height="450" style="width: 19%;">
    <img src="budgets_moneymap.jpg" alt="Bug 2" height="450" style="width: 19%;">
    <img src="expenses_s_moneymap.jpg" alt="Bug 1" height="450" style="width: 19%;">
    <img src="expenses_d_moneymap.jpg" alt="Bug 1" height="450" style="width: 19%;">
</div>

---

## ✨ Core Features  

### 🗓 **Monthly Overview**  
- Interactive calendar showing **total expenses** vs **total budget**  
- Quick navigation to all key sections  

### 📑 **Categories Management**  
- Customizable categories with:  
  - Personalized names & descriptions  
  - Color-coding system  
- Easy add/edit functionality  

### 💰 **Budget Planning**  
- Set monthly budgets per category  
- Visualize allocations for any selected month & year  

### 💸 **Expense Tracking**  
#### Summary View:  
- Progress bars showing spending vs budget on each category
- Filter by month & year
  
#### Detailed View:  
- Daily expense breakdown  
- Add new expenses with:  
  - Date picker  
  - Category selector  
  - Amount input  

### 📊 **Profile Dashboard**  
- Custom profile avatar  
- Circular chart displaying spending distribution  
- Monthly overview at a glance  

---

## 🎨 Design Principles  
✅ **Clean minimalist interface**  
✅ **Intuitive navigation**  
✅ **Visual data representation**  
✅ **Consistent color system**  

---

## 🖥️ **Tech Stack:**  
Android Studio IDE (Java) + Firebase (Database)  

---

## 🔧 Fastbot2 Testing Integration  

1. **Set Up WSL & Dependencies**:  
     ```bash
    # Install Android Command Line Tools  
    wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -P /tmp
    unzip /tmp/commandlinetools-linux-*.zip -d ~/android-sdk/cmdline-tools
    mv ~/android-sdk/cmdline-tools/cmdline-tools ~/android-sdk/cmdline-tools/latest  
    
    # Set Environment Variables  
    echo 'export ANDROID_SDK_ROOT="$HOME/android-sdk"' >> ~/.bashrc  
    echo 'export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"' >> ~/.bashrc  
    source ~/.bashrc  
    
    # Install Required SDK Components  
   yes | sdkmanager --sdk_root=$ANDROID_SDK_ROOT --install "platform-tools" "build-tools;33.0.2"
   yes | sdkmanager --sdk_root=$ANDROID_SDK_ROOT --install "cmake;3.18.1"
   yes | sdkmanager --sdk_root=$ANDROID_SDK_ROOT --install "ndk;25.2.9519653"
   yes | sdkmanager --licenses  
     
     ```  

2. **Install Build Tools & Java**:  
     ```bash
    # Update packages and install essentials  
    sudo apt update  
    sudo apt install -y cmake make g++ openjdk-11-jdk  
    
    # Verify installations  
    cmake --version  # Should show 3.18.1  
    java -version    # Should show OpenJDK 11
     
      ```  

3. **Build Fastbot2**:  
     ```bash
    # Navigate to Fastbot_Android folder  
    cd /path/to/Fastbot_Android  
    
    # Generate Gradle Wrapper  
    gradle wrapper  
    
    # Build JAR files  
    ./gradlew clean makeJar  
    
    # Build Native Libraries  
    sh ./build_native.sh  
     
    ```  

4. **Prepare the Emulator/Device**:  
     ```bash
     # Install ADB  
    sudo apt install adb 
    sudo apt install android-emulator
     
    # Check connected devices
    cd /path/to/AppData/Local/Android/Sdk/platform-tools
    adb.exe devices  
     
     ```  

5. **Deploy Fastbot2 to Device**:  
     ```bash
   # Push JARs and native libraries  
    adb push monkeyq.jar /sdcard/  
    adb push fastbot-thirdpart.jar /sdcard/
    adb push libs/armeabi-v7a/libfastbot.so /data/local/tmp/
    adb push libs/armeabi-v7a/libfastbot_native.so /data/local/tmp/  
    
    # Verify target app package  
    adb shell pm list packages | grep moneymap
     
      ```  

6. **Run Fastbot2 Tests**:  
     ```bash
   # Execute Fastbot (60 minutes, 300ms throttle)  
    adb shell CLASSPATH=/sdcard/monkeyq.jar:/sdcard/fastbot-thirdpart.jar \  
    app_process /system/bin com.android.commands.monkey.Monkey \  
    -p your.app.package --throttle 300 --running-minutes 60  
    
    # Save logs to a file  
    adb shell "CLASSPATH=/sdcard/monkeyq.jar app_process /system/bin com.android.commands.monkey.Monkey \  
    -p your.app.package --throttle 500 --running-minutes 60" > fastbot_logs.txt 2>&1     
     
     ```



