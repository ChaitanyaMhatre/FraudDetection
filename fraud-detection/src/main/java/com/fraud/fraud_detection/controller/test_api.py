import requests
import sys

BASE_URL = "http://localhost:8080"

def run_tests():
    print("[START] Starting API Validation Tests...")
    
    # 1. Login as user to get token and ID
    login_res = requests.post(f"{BASE_URL}/api/auth/login", json={
        "username": "user",
        "password": "user123"
    })
    
    if login_res.status_code != 200:
        print("[FAIL] User login failed:", login_res.json())
        sys.exit(1)
        
    user_info = login_res.json()
    token = user_info["token"]
    user_id = user_info["id"]
    headers = {"Authorization": f"Bearer {token}"}
    print(f"[OK] User logged in. ID={user_id}")
    
    # 2. Login as admin to get admin ID
    admin_login_res = requests.post(f"{BASE_URL}/api/auth/login", json={
        "username": "admin",
        "password": "admin123"
    })
    
    if admin_login_res.status_code != 200:
        print("[FAIL] Admin login failed:", admin_login_res.json())
        sys.exit(1)
        
    admin_id = admin_login_res.json()["id"]
    print(f"[OK] Admin logged in. ID={admin_id}")
    
    # 3. Test Case 1: Self-transfer
    res = requests.post(f"{BASE_URL}/api/transactions/send", headers=headers, json={
        "senderId": user_id,
        "receiverId": user_id,
        "amount": 100,
        "transactionFrequency": 1,
        "avgTransaction": 3000,
        "deviceChange": 0,
        "locationRisk": 0,
        "latitude": 12.9716,
        "longitude": 77.5946
    })
    print(f"Test Case 1 (Self-transfer): Status={res.status_code}, Body={res.json()}")
    assert res.status_code == 400
    assert "Self-transfers are not allowed" in res.json().get("message", "")
    print("[OK] Self-transfer blocked correctly.")
    
    # 4. Test Case 2: Insufficient balance
    res = requests.post(f"{BASE_URL}/api/transactions/send", headers=headers, json={
        "senderId": user_id,
        "receiverId": admin_id,
        "amount": 9999999,
        "transactionFrequency": 1,
        "avgTransaction": 3000,
        "deviceChange": 0,
        "locationRisk": 0,
        "latitude": 12.9716,
        "longitude": 77.5946
    })
    print(f"Test Case 2 (Insufficient balance): Status={res.status_code}, Body={res.json()}")
    assert res.status_code == 400
    assert "Insufficient balance" in res.json().get("message", "")
    print("[OK] Insufficient balance blocked correctly.")

    # 5. Test Case 3: Invalid recipient
    res = requests.post(f"{BASE_URL}/api/transactions/send", headers=headers, json={
        "senderId": user_id,
        "receiverId": 9999,
        "amount": 100,
        "transactionFrequency": 1,
        "avgTransaction": 3000,
        "deviceChange": 0,
        "locationRisk": 0,
        "latitude": 12.9716,
        "longitude": 77.5946
    })
    print(f"Test Case 3 (Invalid recipient): Status={res.status_code}, Body={res.json()}")
    assert res.status_code == 400
    assert "Recipient account not found" in res.json().get("message", "")
    print("[OK] Invalid recipient blocked correctly.")

    # 6. Test Case 4: Valid transaction to trigger OTP
    res = requests.post(f"{BASE_URL}/api/transactions/send", headers=headers, json={
        "senderId": user_id,
        "receiverId": admin_id,
        "amount": 100,
        "transactionFrequency": 1,
        "avgTransaction": 3000,
        "deviceChange": 0,
        "locationRisk": 0,
        "latitude": 12.9716,
        "longitude": 77.5946
    })
    print(f"Test Case 4 (Valid Tx): Status={res.status_code}, StatusField={res.json().get('status')}")
    assert res.status_code == 200
    tx_id = res.json()["id"]
    assert res.json()["status"] == "SUSPICIOUS"
    print("[OK] Valid suspicious transaction triggered OTP setup.")
    
    # 7. Test Case 5: Verify incorrect OTP attempts limit
    print(f"Submitting incorrect OTPs for Transaction ID: {tx_id}...")
    for attempt in range(1, 6):
        verify_res = requests.post(f"{BASE_URL}/api/otp/verify", headers=headers, json={
            "transactionId": tx_id,
            "otp": "999999" # wrong OTP
        })
        print(f"Attempt {attempt}: Status={verify_res.status_code}, Body={verify_res.json()}")
        assert verify_res.status_code == 400
        # Beyond 3 attempts, it should fail
        if attempt > 3:
            assert "Invalid OTP" in verify_res.json().get("message", "")

    print("[SUCCESS] ALL API TESTS COMPLETED SUCCESSFULLY!")

if __name__ == "__main__":
    run_tests()
