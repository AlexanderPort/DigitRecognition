import tensorflow as tf


tf.keras.utils.get_custom_objects().update(
    {"LeakyReLU": tf.keras.layers.LeakyReLU()})
tf.keras.utils.get_custom_objects().update(
    {"Softmax": tf.keras.layers.Softmax()})
model = tf.keras.models.load_model("model2.h5")
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
model.summary()

with open('model2.tflite', 'wb') as f:
    f.write(tflite_model)