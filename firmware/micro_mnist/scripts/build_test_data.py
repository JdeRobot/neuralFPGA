import tensorflow as tf
import argparse

ap = argparse.ArgumentParser()
ap.add_argument("-i", "--images_path", type=str, default='test_images.bin', help="path to images file")
ap.add_argument("-l", "--labels_path", type=str, default='test_labels.bin', help="path to labels file")
args = ap.parse_args()

# Load MNIST dataset
(train_images, train_labels), (test_images, test_labels) = tf.keras.datasets.mnist.load_data()

test_subset = tf.data.Dataset.from_tensor_slices((test_images, test_labels)).shuffle(len(test_images)).take(100)

with open(args.images_path, mode='wb') as test_images_file, open(args.labels_path, mode='wb') as test_labels_file:
    for i in test_subset:
       test_images_file.write(i[0].numpy().tobytes())
       test_labels_file.write(i[1].numpy().tobytes())