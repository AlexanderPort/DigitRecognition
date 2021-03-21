import random
import numpy as np
import tensorflow as tf
import matplotlib.pyplot as plt


x_train = np.load("train_images.npy")
y_train = np.load("train_labels.npy")

print("x_train shape =", x_train.shape)
print("y_train shape =", y_train.shape)

num_samples = x_train.shape[0]

classes = {
    0: "0",
    1: "1",
    2: "2",
    3: "3",
    4: "4",
    5: "5",
    6: "6",
    7: "7",
    8: "8",
    9: "9",
    10: "A",
    11: "B",
    12: "C",
    13: "D",
    14: "E",
    15: "F",
    16: "G",
    17: "H",
    18: "I",
    19: "J",
    20: "K",
    21: "L",
    22: "M",
    23: "N",
    24: "O",
    25: "P",
    26: "Q",
    27: "R",
    28: "S",
    29: "T",
    30: "U",
    31: "V",
    32: "W",
    33: "X",
    34: "Y",
    35: "Z",
    36: "a",
    37: "b",
    38: "d",
    39: "e",
    40: "f",
    41: "g",
    42: "h",
    43: "n",
    44: "q",
    45: "r",
    46: "t"
}


def preprocess_x_train(x_train: np.ndarray) -> tf.Variable:
    x_train = (x_train - 255 / 2) / (255 / 2)
    x_train = tf.Variable(x_train)
    x_train = tf.transpose(x_train, perm=(0, 2, 1))
    x_train = x_train[..., tf.newaxis]
    x_train = tf.image.grayscale_to_rgb(x_train)
    x_train = tf.image.resize(x_train, size=(32, 32))
    return x_train


x_train = preprocess_x_train(x_train)

rows, columns, seen = 10, 10, set()
figure = plt.figure(figsize=(rows, columns))
for i in range(rows):
    for j in range(columns):
        index = random.randint(0, num_samples)
        image, label = x_train[index], y_train[index]
        minimum = tf.reduce_min(image)
        maximum = tf.reduce_max(image)
        image = (image - minimum) / (maximum - minimum)
        subplot = figure.add_subplot(
            rows, columns, i * columns + j + 1)
        subplot.set_title(classes[label])
        plt.imshow(image)

plt.show()


model = tf.keras.models.Sequential([
    tf.keras.layers.Input(shape=(32, 32, 3)),
    tf.keras.layers.Conv2D(filters=32, kernel_size=(3, 3), padding="same",
                           activation=tf.keras.layers.LeakyReLU()),
    tf.keras.layers.AveragePooling2D(pool_size=(2, 2)),
    tf.keras.layers.Conv2D(filters=64, kernel_size=(3, 3), padding="same",
                           activation=tf.keras.layers.LeakyReLU()),
    tf.keras.layers.AveragePooling2D(pool_size=(2, 2)),
    tf.keras.layers.Conv2D(filters=128, kernel_size=(3, 3), padding="same",
                           activation=tf.keras.layers.LeakyReLU()),
    tf.keras.layers.AveragePooling2D(pool_size=(2, 2)),
    tf.keras.layers.Conv2D(filters=256, kernel_size=(3, 3), padding="same",
                           activation=tf.keras.layers.LeakyReLU()),
    tf.keras.layers.AveragePooling2D(pool_size=(2, 2)),
    tf.keras.layers.Conv2D(filters=512, kernel_size=(3, 3), padding="same",
                           activation=tf.keras.layers.LeakyReLU()),
    tf.keras.layers.AveragePooling2D(pool_size=(2, 2)),
    tf.keras.layers.Flatten(),
    tf.keras.layers.Dense(units=128, activation=tf.keras.layers.LeakyReLU()),
    tf.keras.layers.Dense(units=len(classes), activation=tf.keras.layers.Softmax())
])

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.003),
    loss=tf.keras.losses.SparseCategoricalCrossentropy(),
    metrics=[tf.keras.metrics.SparseCategoricalAccuracy()]
)

model.fit(x_train, y_train, batch_size=100, epochs=3, shuffle=True)

model.summary()

model.save("model.h5")