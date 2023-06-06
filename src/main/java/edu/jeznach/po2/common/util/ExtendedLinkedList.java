package edu.jeznach.po2.common.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Extends standard {@link LinkedList}, offering unique methods for rest
 * of possible actions.
 * <br><br>
 * <blockquote>
 * <table border="1">
 *   <caption>Unique-identified methods that are advised are:</caption>
 *   <tr>
 *     <th>Element<br>\Action</th>
 *     <th>First</th>
 *     <th>Last</th>
 *   </tr>
 *   <tr>
 *     <td>Add</td>
 *     <td>{@link #push(Object)}</td>
 *     <td>{@link #add(Object)}</td>
 *   </tr>
 *   <tr>
 *     <td>Remove<br>or null</td>
 *     <td>{@link #poll()}</td>
 *     <td>{@link #shift()}</td>
 *   </tr>
 *   <tr>
 *     <td>Remove<br>or exception<br></td>
 *     <td>{@link #pop()}</td>
 *     <td>{@link #eject()}</td>
 *   </tr>
 *   <tr>
 *     <td>Get<br>or null</td>
 *     <td>{@link #peek()}</td>
 *     <td>{@link #look()}</td>
 *   </tr>
 *   <tr>
 *     <td>Get<br>or exception</td>
 *     <td>{@link #element()}</td>
 *     <td>{@link #item()}</td>
 *   </tr>
 * </table>
 * </blockquote>
 * @param <E> the type of elements held in this collection
 * @see LinkedList
 */
public class ExtendedLinkedList<E> extends LinkedList<E> {

    /**
     * Appends the specified element to the end of this list.
     *
     * <p>This method is equivalent to {@link #addLast}.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */
    @Override
    public boolean add(E e) {
        return super.add(e);
    }

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     *
     * @return the head of this list, or {@code null} if this list is empty
     * @since 1.5
     */
    @Override
    public E peek() {
        return super.peek();
    }

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     *
     * @return the head of this list
     * @throws NoSuchElementException if this list is empty
     * @since 1.5
     */
    @Override
    public E element() {
        return super.element();
    }

    /**
     * Retrieves and removes the head (first element) of this list.
     *
     * @return the head of this list, or {@code null} if this list is empty
     * @since 1.5
     */
    @Override
    public E poll() {
        return super.poll();
    }

    /**
     * Pushes an element onto the stack represented by this list.  In other
     * words, inserts the element at the front of this list.
     *
     * <p>This method is equivalent to {@link #addFirst}.
     *
     * @param e the element to push
     * @since 1.6
     */
    @Override
    public void push(E e) {
        super.push(e);
    }

    /**
     * Pops an element from the stack represented by this list.  In other
     * words, removes and returns the first element of this list.
     *
     * <p>This method is equivalent to {@link #removeFirst()}.
     *
     * @return the element at the front of this list (which is the top
     * of the stack represented by this list)
     * @throws NoSuchElementException if this list is empty
     * @since 1.6
     */
    @Override
    public E pop() {
        return super.pop();
    }

    /**
     * Retrieves and removes the tail (last element) of this list.
     *
     * @return the tail of this list, or {@code null} if this list is empty
     */
    public E shift() { return pollLast(); }

    /**
     * Ejects an element from the bottom of stack represented by this list.
     * In other words, removes and returns the last element of this list.
     *
     * <p>This method is equivalent to {@link #removeLast()}.
     *
     * @return the element at the back of this list (which is the bottom
     *         of the stack represented by this list)
     * @throws NoSuchElementException if this list is empty
     */
    public E eject() { return removeLast(); }

    /**
     * Retrieves, but does not remove, the tail (last element) of this list.
     *
     * @return the tail of this list, or {@code null} if this list is empty
     */
    public E look() { return peekLast(); }

    /**
     * Retrieves, but does not remove, the tail (last element) of this list.
     *
     * @return the tail of this list
     * @throws NoSuchElementException if this list is empty
     */
    public E item() { return pollLast(); }
}
