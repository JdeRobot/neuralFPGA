/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
#ifndef TENSORFLOW_LITE_EXPERIMENTAL_MICRO_PROFILING_DEBUG_PROFILER_H_
#define TENSORFLOW_LITE_EXPERIMENTAL_MICRO_PROFILING_DEBUG_PROFILER_H_

#include <ctime>

#include "tensorflow/lite/core/api/profiler.h"
#include "tensorflow/lite/micro/compatibility.h"

namespace tflite {
namespace profiling {

constexpr uint32_t kInvalidEventHandle = static_cast<uint32_t>(~0) - 1;

// A profiling event.
struct ProfileEvent {
  // Describes the type of event.
  // The event_metadata field may contain additional data for interpreting
  // the event.
  using EventType = tflite::Profiler::EventType;

  // Label of the event. This usually describes the event.
  const char* tag;
  // Timestamp when the event began.
  clock_t begin_timestamp;
  // Timestamp when the event ended.
  clock_t end_timestamp;
  // The field containing the type of event. This must be one of the event types
  // in EventType.
  EventType event_type;
  // Extra data describing the details of the event.
  int64_t event_metadata;
  // The index of subgraph where an event came from.
  int64_t event_subgraph_index;
};

template <size_t bufferSize>
class BufferedProfiler : public tflite::Profiler {
 public:
  BufferedProfiler(ErrorReporter* error_reporter, bool enabled = false): enabled_(enabled), current_index_(0) {}

  uint32_t BeginEvent(const char* tag, EventType event_type, int64_t event_metadata, int64_t event_subgraph_index) override {
    if (!enabled_) {
      return kInvalidEventHandle;
    }
    
    if (current_index_ >= bufferSize) {
      error_reporter_->Report("ProfileBuffer: event buffer full.\n");
      return kInvalidEventHandle;
    }

    uint32_t index = current_index_;
    event_buffer_[index].tag = tag;
    event_buffer_[index].event_type = event_type;
    event_buffer_[index].event_metadata = event_metadata;
    event_buffer_[index].event_subgraph_index = event_subgraph_index;
    event_buffer_[index].begin_timestamp = clock();//FIXME: see time::NowMicros();
    event_buffer_[index].end_timestamp = 0;
    current_index_++;
    return index;
  }

  void EndEvent(uint32_t event_handle) override {
    if (!enabled_ || event_handle == kInvalidEventHandle ||
        event_handle > current_index_ || event_handle >= bufferSize) {
      return;
    }

    event_buffer_[event_handle].end_timestamp = clock();//FIXME: see time::NowMicros();
  }

  void StartProfiling() { enabled_ = true; }
  void StopProfiling() { enabled_ = false; }
  void Reset() { enabled_ = false; current_index_ = 0; }

  // Returns the size of the buffer.
  size_t Size() const { return current_index_; }

  // Returns the profile event at the given index. If the index is invalid a
  // nullptr is returned. The return event may get overwritten if more events
  // are added to buffer.
  const struct ProfileEvent* At(size_t index) const {
    size_t size = Size();
    if (index >= size) {
      return nullptr;
    }
    return &event_buffer_[index];
  }

 private:
  bool enabled_;
  uint32_t current_index_;
  ProfileEvent event_buffer_[bufferSize];
  ErrorReporter* error_reporter_;

  TF_LITE_REMOVE_VIRTUAL_DELETE;
};

}  // namespace profiling
}  // namespace tflite

#endif  // TENSORFLOW_LITE_EXPERIMENTAL_MICRO_PROFILING_DEBUG_PROFILER_H_
