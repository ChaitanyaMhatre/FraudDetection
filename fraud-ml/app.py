from flask import Flask, request, jsonify
import joblib
import pandas as pd

# 🚀 Create Flask app
app = Flask(__name__)

# 🧠 Load trained AI model
model = joblib.load("fraud_model.pkl")

# 🎯 Prediction API
@app.route('/predict', methods=['POST'])
def predict():

    data = request.json

    amount = data['amount']

    transaction_frequency = data['transaction_frequency']

    avg_transaction = data['avg_transaction']

    device_change = data['device_change']

    location_risk = data['location_risk']

    # 📊 DataFrame
    input_data = pd.DataFrame([[
        amount,
        transaction_frequency,
        avg_transaction,
        device_change,
        location_risk
    ]], columns=[
        'amount',
        'transaction_frequency',
        'avg_transaction',
        'device_change',
        'location_risk'
    ])

    # 🤖 AI Prediction
    prediction = int(model.predict(input_data)[0])

    # 📈 Confidence Score
    probabilities = model.predict_proba(input_data)[0]

    # ✅ Probability of FRAUD class
    confidence = round(
    probabilities[1] * 100,
    2
    )

    # 🚨 Fraud Reasons
    reasons = []

    if amount > 50000:
        reasons.append("High transaction amount")

    if transaction_frequency > 10:
        reasons.append("Suspicious transaction frequency")

    if amount > avg_transaction * 3:
        reasons.append("Transaction exceeds normal behavior")

    if device_change == 1:
        reasons.append("New device detected")

    if location_risk == 1:
        reasons.append("Risky location detected")

    return jsonify({
        'prediction': prediction,
        'confidence': confidence,
        'reasons': reasons
    })

# ▶ Start server
if __name__ == '__main__':
    app.run(port=5000)