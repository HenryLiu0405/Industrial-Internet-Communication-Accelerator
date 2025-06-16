# 🚀Industrial Internet Communication Accelerator 

An open-source software suite designed to **accelerate communication** for industrial internet applications. It includes **three Java-based apps** for mobile and server sides, each with specific roles to ensure fast and reliable connectivity.

---

## 🧩 Project Structure

This project consists of **three main applications**:


### 📱 1. Terminal-Side Android App – `AirSpeed`

- **Path:** `androidstudioProjects/AirSpeed`  
- **Environment:** Android Studio  
- **Language:** Java  
- **Functionality:**
  - Runs on Android phones
  - Collects **five-tuple information** of the apps that require acceleration
  - Sends the five-tuple to the server

> **Note:** The five-tuple includes: `Source IP`, `Destination IP`, `Source Port`, `Destination Port`, and `Protocol`.


### 🖥️ 2. Server-Side Java Application

- **Path:** `NetBeansProjects/`  
- **Environment:** NetBeans  
- **Language:** Java  
- **Functionality:**
  - Runs on a **server or high-performance PC**
  - Receives five-tuple data from the mobile app
  - Forwards the data to the **telecom operator’s server**, allowing them to recognize which app needs acceleration


### 📱 3. Terminal-Side Android App – `SpeedTest`

- **Path:** `androidstudioProjects/SpeedTest`  
- **Environment:** Android Studio  
- **Language:** Java  
- **Functionality:**
  - Runs on Android phones
  - Tests whether the mobile device can successfully communicate with the server

---

## 🛠 Technologies Used

- **Java** – Core programming language  
- **Android Studio** – Mobile development IDE  
- **NetBeans** – Server-side development IDE  
- **Industrial Internet** – Application domain focused on real-time connectivity

---

## 🏁 Getting Started

To run the applications:

```bash
# Clone the repository
git clone https://github.com/HenryLiu0405/Industrial-Internet-Communication-Accelerator.git
