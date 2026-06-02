import pandas as pd
import random

from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier

import joblib

# 🚀 Generate advanced dataset
data = []

for i in range(5000):

    amount = random.randint(100, 500000)

    transaction_frequency = random.randint(1, 20)

    avg_transaction = random.randint(1000, 50000)

    device_change = random.randint(0, 1)

    location_risk = random.randint(0, 1)

    # 🎯 Fraud scoring
    fraud_score = 0

    if amount > 50000:
        fraud_score += 2

    if transaction_frequency > 10:
        fraud_score += 1

    if amount > avg_transaction * 3:
        fraud_score += 2

    if device_change == 1:
        fraud_score += 1

    if location_risk == 1:
        fraud_score += 1

    is_fraud = 1 if fraud_score >= 4 else 0

    data.append([
        amount,
        transaction_frequency,
        avg_transaction,
        device_change,
        location_risk,
        is_fraud
    ])

# 📊 DataFrame
df = pd.DataFrame(data, columns=[
    'amount',
    'transaction_frequency',
    'avg_transaction',
    'device_change',
    'location_risk',
    'is_fraud'
])

# 🎯 Features
X = df[[
    'amount',
    'transaction_frequency',
    'avg_transaction',
    'device_change',
    'location_risk'
]]

# 🎯 Labels
y = df['is_fraud']

# ✂ Split
X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.2,
    random_state=42
)

# 🤖 Advanced AI model
model = RandomForestClassifier(
    n_estimators=200,
    random_state=42
)

# 🎓 Train
model.fit(X_train, y_train)

# 💾 Save model
joblib.dump(model, "fraud_model.pkl")

print("✅ Advanced Behavioral AI Model Trained")